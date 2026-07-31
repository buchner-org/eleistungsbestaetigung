package de.yoshteq.dcs.fhir.model;

import static de.yoshteq.dcs.fhir.FhirHelper.assertFixed;
import static de.yoshteq.dcs.fhir.FhirHelper.assertThat;
import static de.yoshteq.dcs.fhir.FhirHelper.required;

import java.util.UUID;

import org.hl7.fhir.r4.model.ChargeItem.ChargeItemStatus;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import de.yoshteq.dcs.fhir.ExtensionHelper;
import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.IdentifierHelper;
import de.yoshteq.dcs.fhir.MetaHelper;

public class ChargeItem {
	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ChargeItem";

	private static final String POLICY_VERSION_URL = "https://e-lb.de/fhir/CodeSystem/KK_ELB_CS_Nutzungsbedingung_Version";

	private static final String PAYOR_EXTENSION_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_Payor";

	private static final String CODE_URL = "https://e-lb.de/fhir/sid/KK_ELB_ChargeItemCode";

	public enum Status {
		/**
		 * Billable
		 * 
		 * <p>
		 * The charge item is ready for billing.
		 */
		BILLABLE(ChargeItemStatus.BILLABLE),

		/**
		 * Not billable
		 * 
		 * <p>
		 * The charge item has been determined to be not billable (e.g. due to rules
		 * associated with the billing code).
		 */
		NOT_BILLABLE(ChargeItemStatus.NOTBILLABLE),

		/**
		 * Unknown
		 * 
		 * <p>
		 * The authoring system does not know which of the status values currently
		 * applies for this charge item Note: This
		 * concept is not to be used for "other" - one of the listed statuses is
		 * presumed to apply, it's just not known which one.
		 */
		UNKNOWN(ChargeItemStatus.UNKNOWN),

		/**
		 * Aborted
		 * 
		 * <p>
		 * The processing of the charge was aborted.
		 */
		ABORTED(ChargeItemStatus.ABORTED);

		private ChargeItemStatus chargeItemStatus;

		private Status(ChargeItemStatus status) {
			this.chargeItemStatus = status;
		}

		public ChargeItemStatus getChargeItemStatus() {
			return chargeItemStatus;
		}

		public static Status from(ChargeItemStatus status) {
			for (var value : Status.values()) {
				if (value.chargeItemStatus == status) {
					return value;
				}
			}

			throw new FhirValidationException("Unsupported chargeItem status: " + status.toCode());
		}
	}

	public static ChargeItem from(org.hl7.fhir.r4.model.ChargeItem chargeItem) {
		return new ChargeItem(chargeItem);
	}

	public static ChargeItem create(
			Integer policyVersion,
			String patientKvnr,
			Integer kostentraegerIK,
			String serviceCode,
			Occurrence occurrence,
			Performer performer) {
		return create(policyVersion, UUID.randomUUID(), patientKvnr, kostentraegerIK, serviceCode, occurrence,
				performer);
	}

	public static ChargeItem create(
			Integer policyVersion,
			UUID uuid,
			String patientKvnr,
			Integer kostentraegerIK,
			String serviceCode,
			Occurrence occurrence,
			Performer performer) {
		return create(policyVersion, uuid, Status.UNKNOWN, patientKvnr, kostentraegerIK, serviceCode, occurrence,
				performer);
	}

	public static ChargeItem create(
			Integer policyVersion,
			UUID uuid,
			Status status,
			String patientKvnr,
			Integer kostentraegerIK,
			String serviceCode,
			Occurrence occurrence,
			Performer performer) {
		return new ChargeItem(policyVersion, uuid, status, patientKvnr, kostentraegerIK, serviceCode, occurrence,
				performer);
	}

	private static CodeableConcept chargeItemCodeWith(String code) {
		var coding = new Coding()
				.setSystem(CODE_URL)
				.setCode(code);
		return new CodeableConcept().addCoding(coding);
	}

	private final org.hl7.fhir.r4.model.ChargeItem fhirChargeItem;

	private UUID uuid;

	private Status status;

	private String patientKvnr;

	private Integer kostentraegerIK;

	private String code;

	private Occurrence occurrence;

	private Performer performer;

	private Integer policyVersion;

	public Integer getPolicyVersion() {
		return policyVersion;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Status getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getPatientKvnr() {
		return patientKvnr;
	}

	public Integer getKostentraegerIK() {
		return kostentraegerIK;
	}

	public Occurrence getOccurrence() {
		return occurrence;
	}

	public Performer getPerformer() {
		return performer;
	}

	private ChargeItem(
			Integer policyVersion,
			UUID uuid,
			Status status,
			String patientKvnr,
			Integer kostentraegerIK,
			String code,
			Occurrence occurance,
			Performer performer) {
		super();
		this.policyVersion = policyVersion;
		this.uuid = uuid;
		this.status = status;
		this.patientKvnr = patientKvnr;
		this.kostentraegerIK = kostentraegerIK;
		this.code = code;
		this.occurrence = occurance;
		this.performer = performer;

		this.fhirChargeItem = null;
	}

	private ChargeItem(org.hl7.fhir.r4.model.ChargeItem chargeItem) {
		this.fhirChargeItem = chargeItem;

		MetaHelper.assertProfile(chargeItem, PROFILE_URL);

		extractPolicyVersion();
		extractUuid();
		extractStatus();
		extractCode();
		extractSubject();
		extractPayorExtension();
		extractOccurrence();
		extractPerformer();
	}

	private void extractPolicyVersion() {
		if (fhirChargeItem.getMeta().getSecurity().isEmpty()) {
			policyVersion = 0;
		} else {
			var security = fhirChargeItem.getMeta().getSecurity().get(0);
			assertFixed(POLICY_VERSION_URL, security.getSystem());
			var policyVersionCode = required(security::getCode, "policy version code missing");
			policyVersion = required(() -> Integer.valueOf(policyVersionCode),
					"policy version must be numeric [chargeItem.meta.security.code]");

		}

		assertThat(() -> policyVersion <= 1, "invalid policy version [chargeItem.meta.security.code]");
	}

	private void extractUuid() {
		uuid = IdentifierHelper.getUuid(required(() -> fhirChargeItem.getIdentifier().get(0)));
	}

	private void extractStatus() {
		var statusValue = required(() -> fhirChargeItem.getStatus());
		this.status = Status.from(statusValue);
	}

	private void extractCode() {
		assertFixed(CODE_URL, () -> fhirChargeItem.getCode().getCoding().get(0).getSystem());

		code = required(() -> fhirChargeItem.getCode().getCoding().get(0).getCode());
	}

	private void extractSubject() {
		var subjectIdentifier = required(() -> fhirChargeItem.getSubject().getIdentifier());
		patientKvnr = IdentifierHelper.getKvnrValue(subjectIdentifier);
	}

	private void extractPayorExtension() {
		var subjectAssignerIdentifier = ExtensionHelper.getExtensionWithUrl(fhirChargeItem, PAYOR_EXTENSION_URL,
				Identifier.class);
		kostentraegerIK = IdentifierHelper.getIkValue(subjectAssignerIdentifier);
	}

	private void extractOccurrence() {
		var occurrenceElement = required(() -> fhirChargeItem.getOccurrence());
		this.occurrence = Occurrence.from(occurrenceElement);
	}

	private void extractPerformer() {
		performer = Performer.from(fhirChargeItem.getPerformer());
	}

	public org.hl7.fhir.r4.model.ChargeItem toChargeItem() {
		var chargeItem = new org.hl7.fhir.r4.model.ChargeItem();

		var meta = MetaHelper.withProfileUrl(PROFILE_URL);
		if (policyVersion != null && policyVersion >= 0) {
			var securityCoding = new Coding()
					.setSystem(POLICY_VERSION_URL)
					.setCode(policyVersion.toString());
			meta.addSecurity(securityCoding);
		}
		chargeItem.setMeta(meta);

		chargeItem.addIdentifier(IdentifierHelper.createUuidIdentifier(uuid));
		chargeItem.setStatus(status.getChargeItemStatus());
		chargeItem.setCode(chargeItemCodeWith(code));
		chargeItem.setSubject(new Reference().setIdentifier(IdentifierHelper.createKvnrIdentifier(patientKvnr)));

		var payorExtension = new Extension()
				.setUrl(PAYOR_EXTENSION_URL)
				.setValue(IdentifierHelper.createIkIdentifier(kostentraegerIK));
		chargeItem.addExtension(payorExtension);

		chargeItem.setOccurrence(occurrence.toOccurance());

		if (performer != null) {
			chargeItem.getPerformer().addAll(performer.toPerformers());
		}

		return chargeItem;
	}

}
