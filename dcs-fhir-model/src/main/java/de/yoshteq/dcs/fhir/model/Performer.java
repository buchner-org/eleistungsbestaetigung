package de.yoshteq.dcs.fhir.model;

import static de.yoshteq.dcs.fhir.FhirHelper.assertFixed;
import static de.yoshteq.dcs.fhir.FhirHelper.assertThat;
import static de.yoshteq.dcs.fhir.FhirHelper.required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.ChargeItem.ChargeItemPerformerComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.IdentifierHelper;

public class Performer {

	private static final String TYPE_PRACTITIONER = Practitioner.class.getSimpleName();

	private static final String TYPE_ORGANIZATION = Organization.class.getSimpleName();

	private static final String FUNCTION_CODE_SYSTEM = "https://e-lb.de/fhir/CodeSystem/KK_ELB_SGS";

	private static final String LBNR_SYSTEM = "http://fhir.de/sid/bfarm/lbnr";

	public static Performer from(List<ChargeItemPerformerComponent> performers) {
		return new Performer(performers);
	}

	public static Performer organization(
			FunctionCode function,
			Integer organizationIK,
			String organizationDisplayValue) {
		return organizationWithPractitioner(function, organizationIK, organizationDisplayValue, null, null);
	}

	public static Performer organizationWithPractitioner(
			FunctionCode function,
			Integer organizationIK,
			String organizationDisplayValue,
			String practitionerIdentifierValue,
			String practitionerDisplayValue) {
		return new Performer(function, organizationIK, organizationDisplayValue, practitionerIdentifierValue,
				practitionerDisplayValue);
	}

	public enum FunctionCode {
		HEBAMME("F"),
		HEILMITTEL("B"),
		HILFMITTEL("A"),
		HKP("C"),
		AKI("R");

		private String value;

		private FunctionCode(String value) {
			this.value = value;
		}

		public static FunctionCode of(String value) {
			for (var function : FunctionCode.values()) {
				if (function.value.equals(value)) {
					return function;
				}
			}

			throw new FhirValidationException("Unknown function value of performer: " + value);
		}

		public String getValue() {
			return value;
		}
	}

	private FunctionCode function;

	private Integer organizationIK;

	private String organizationDisplayValue;

	private String practitionerIdentifierValue;

	private String practitionerDisplayValue;

	private Performer(
			FunctionCode function,
			Integer organizationIK,
			String organizationDisplayValue,
			String practitionerIdentifierValue,
			String practitionerDisplayValue) {
		super();
		this.function = function;
		this.organizationIK = organizationIK;
		this.organizationDisplayValue = organizationDisplayValue;
		this.practitionerIdentifierValue = practitionerIdentifierValue;
		this.practitionerDisplayValue = practitionerDisplayValue;
	}

	private Performer(List<ChargeItemPerformerComponent> performers) {
		extractPerformerOrganization(performers);
		extractPerformerPractitioner(performers);
	}

	private void extractPerformerOrganization(List<ChargeItemPerformerComponent> performers) {
		var performerOrganization = getPerformerForType(performers, TYPE_ORGANIZATION)
				.orElseThrow(
						() -> new FhirValidationException("Cannot find Performer of type Organization in ChargeItem."));
		var organisationIdentifier = required(performerOrganization.getActor().getIdentifier());

		function = FunctionCode.of(
				required(
						() -> performerOrganization.getFunction().getCoding().get(0).getCode(),
						"Performer function of organization is missing"));
		organizationIK = IdentifierHelper.getIkValue(organisationIdentifier);
		organizationDisplayValue = required(
				() -> performerOrganization.getActor().getDisplay(),
				"Performer organization display value missing");
	}

	private void extractPerformerPractitioner(List<ChargeItemPerformerComponent> performers) {
		var performerPactitioner = getPerformerForType(performers, TYPE_PRACTITIONER);
		if (function == FunctionCode.HKP || function == FunctionCode.AKI) {
			if (performerPactitioner.isEmpty()) {
				throw new FhirValidationException(
						"Practitioner information is missing [chargeitem.perfomer.practitioner]");
			}

			assertThat(
					() -> performerPactitioner.get().hasActor()
							&& performerPactitioner.get().getActor().hasIdentifier(),
					"[chargeitem.perfomer.practitioner.actor.identifier] LBNR missing");

			assertFixed(LBNR_SYSTEM, () -> performerPactitioner.get().getActor().getIdentifier().getSystem());
			practitionerIdentifierValue = required(
					() -> performerPactitioner.get().getActor().getIdentifier().getValue(),
					"Performer practitioner identifier value missing");
		} else if (performerPactitioner.isEmpty()) {
			return;
		}

		practitionerDisplayValue = required(
				() -> performerPactitioner.get().getActor().getDisplay(),
				"Performer practitioner display value missing");
	}

	private Optional<ChargeItemPerformerComponent> getPerformerForType(
			Collection<ChargeItemPerformerComponent> performerList,
			String type) {
		return performerList.stream()
				.filter(p -> p.getActor().getType().equals(type))
				.findFirst();
	}

	public FunctionCode getFunction() {
		return function;
	}

	public Integer getOrganizationIK() {
		return organizationIK;
	}

	public String getOrganizationDisplayValue() {
		return organizationDisplayValue;
	}

	public String getPractitionerIdentifierValue() {
		return practitionerIdentifierValue;
	}

	public String getPractitionerDisplayValue() {
		return practitionerDisplayValue;
	}

	public List<ChargeItemPerformerComponent> toPerformers() {
		var performers = new ArrayList<ChargeItemPerformerComponent>();
		performers.add(createPerfomerOrganization());

		if ((practitionerDisplayValue != null && !practitionerDisplayValue.isBlank())
				|| (practitionerIdentifierValue != null && !practitionerIdentifierValue.isBlank())) {
			performers.add(createPerfomerPractitioner());
		}

		return performers;
	}

	private ChargeItemPerformerComponent createPerfomerOrganization() {
		var coding = new Coding()
				.setSystem(FUNCTION_CODE_SYSTEM)
				.setCode(function.value);
		var functionConcept = new CodeableConcept().addCoding(coding);

		var actor = new Reference()
				.setDisplay(organizationDisplayValue)
				.setIdentifier(IdentifierHelper.createIkIdentifier(organizationIK))
				.setType(TYPE_ORGANIZATION);

		return new ChargeItemPerformerComponent()
				.setFunction(functionConcept)
				.setActor(actor);
	}

	private ChargeItemPerformerComponent createPerfomerPractitioner() {
		var actor = new Reference()
				.setDisplay(practitionerDisplayValue)
				.setType(TYPE_PRACTITIONER);

		if (function == FunctionCode.HKP || function == FunctionCode.AKI) {
			var identifier = new Identifier()
					.setValue(practitionerIdentifierValue)
					.setSystem(LBNR_SYSTEM);
			actor.setIdentifier(identifier);
		}

		return new ChargeItemPerformerComponent()
				.setActor(actor);
	}

}
