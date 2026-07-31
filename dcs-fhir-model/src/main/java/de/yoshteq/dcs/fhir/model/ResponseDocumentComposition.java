package de.yoshteq.dcs.fhir.model;

import static de.yoshteq.dcs.fhir.FhirHelper.assertFixed;
import static de.yoshteq.dcs.fhir.FhirHelper.assertThat;
import static de.yoshteq.dcs.fhir.FhirHelper.required;

import java.time.temporal.TemporalAccessor;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionStatus;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.Reference;

import de.yoshteq.dcs.fhir.DateTimeTypeConverter;
import de.yoshteq.dcs.fhir.IdentifierHelper;
import de.yoshteq.dcs.fhir.MetaHelper;

public class ResponseDocumentComposition {

	private static final String FIXED_TITLE = "Elektronische Leistungsbestätigung";

	private static final String TYPE_CODE = "AM190107";

	private static final String TYPE_SYSTEM = "http://dvmd.de/fhir/CodeSystem/kdl";

	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ResponseComposition";

	public static ResponseDocumentComposition from(Composition composition) {
		return new ResponseDocumentComposition(composition);
	}

	public static ResponseDocumentComposition create(Integer bestaetigungsSystemIk, TemporalAccessor date,
			String chargeItemReferenceUrl) {
		return new ResponseDocumentComposition(date, bestaetigungsSystemIk, chargeItemReferenceUrl);
	}

	private final Composition composition;

	private TemporalAccessor date;

	private Integer bestaetigungssystemIk;

	private String chargeItemReferenceUrl;

	private ResponseDocumentComposition(Composition composition) {
		super();
		this.composition = composition;

		validate();
		extractParameters();
	}

	private ResponseDocumentComposition(TemporalAccessor date, Integer bestaetigungsSystemIk,
			String chargeItemReferenceUrl) {
		super();
		this.date = date;
		this.bestaetigungssystemIk = bestaetigungsSystemIk;
		this.chargeItemReferenceUrl = chargeItemReferenceUrl;

		composition = null;
	}

	private void validate() {
		MetaHelper.assertProfile(composition, PROFILE_URL);
		assertFixed(CompositionStatus.FINAL, () -> composition.getStatus());

		assertFixed(TYPE_SYSTEM, () -> composition.getType().getCoding().get(0).getSystem());
		assertFixed(TYPE_CODE, () -> composition.getType().getCoding().get(0).getCode());

		assertFixed(FIXED_TITLE, () -> composition.getTitle());

		assertThat(() -> composition.getAuthor().size() == 1, "only single author is allowed in " + PROFILE_URL);
		assertThat(() -> composition.getSection().size() == 1, "only single section is allowed in " + PROFILE_URL);
		assertThat(() -> composition.getSection().get(0).getEntry().size() == 1,
				"only single entry is allowed in section of " + PROFILE_URL);
	}

	private void extractParameters() {
		date = DateTimeTypeConverter.convertDateTimeTypePrecision(composition.getDateElement());
		bestaetigungssystemIk = IdentifierHelper
				.getIkValue(
						required(() -> composition.getAuthor().get(0).getIdentifier(), "Author identifier is missing"));

		chargeItemReferenceUrl = required(
				() -> composition.getSection().get(0).getEntry().get(0).getReference(),
				"full url of chargeItem is missing");
	}

	public TemporalAccessor getDate() {
		return date;
	}

	public Integer getBestaetigungssystemIk() {
		return bestaetigungssystemIk;
	}

	public String getChargeItemReferenceUrl() {
		return chargeItemReferenceUrl;
	}

	public Composition toComposition() {
		var coding = new Coding()
				.setSystem(TYPE_SYSTEM)
				.setCode(TYPE_CODE);
		var type = new CodeableConcept().addCoding(coding);

		var author = new Reference()
				.setIdentifier(IdentifierHelper.createIkIdentifier(bestaetigungssystemIk));

		var entry = new Reference()
				.setReference(chargeItemReferenceUrl);

		var section = new SectionComponent()
				.addEntry(entry);

		var result = new Composition();
		result.setMeta(MetaHelper.withProfileUrl(PROFILE_URL));
		result.setStatus(CompositionStatus.FINAL);
		result.setType(type);
		result.setDateElement(DateTimeTypeConverter.temporalAccessorToDateTimeType(date));
		result.addAuthor(author);
		result.setTitle(FIXED_TITLE);
		result.addSection(section);

		return result;
	}

}
