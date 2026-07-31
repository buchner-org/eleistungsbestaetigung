package de.yoshteq.dcs.fhir.model;

import static de.yoshteq.dcs.fhir.FhirHelper.assertFixed;
import static de.yoshteq.dcs.fhir.FhirHelper.assertThat;
import static de.yoshteq.dcs.fhir.FhirHelper.required;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.InstantType;

import de.yoshteq.dcs.fhir.DateTimeTypeConverter;
import de.yoshteq.dcs.fhir.IdentifierHelper;
import de.yoshteq.dcs.fhir.MetaHelper;
import de.yoshteq.dcs.fhir.UuidHelper;
import de.yoshteq.dcs.fhir.model.ChargeItem.Status;

public class ResponseDocumentBundle {

	private static final String PROFILE_URL = "https://e-lb.de/fhir/StructureDefinition/KK_ELB_ResponseDocumentBundle";

	public static ResponseDocumentBundle from(Bundle bundle) {
		return new ResponseDocumentBundle(bundle);
	}

	public static ResponseDocumentBundle create(Integer bestaetigungsSystemIk, ChargeItem chargeItem) {
		return create(bestaetigungsSystemIk, UUID.randomUUID(), chargeItem);
	}

	public static ResponseDocumentBundle create(Integer bestaetigungsSystemIk, UUID uuid, ChargeItem chargeItem) {
		var composition = ResponseDocumentComposition.create(bestaetigungsSystemIk, ZonedDateTime.now(),
				UuidHelper.toUuidString(UUID.randomUUID()));
		return new ResponseDocumentBundle(uuid, ZonedDateTime.now(), composition, chargeItem);
	}

	public static ResponseDocumentBundle create(UUID uuid, ZonedDateTime timestamp,
			ResponseDocumentComposition composition, ChargeItem chargeItem) {
		return new ResponseDocumentBundle(uuid, timestamp, composition, chargeItem);
	}

	private final Bundle bundle;

	private UUID uuid;

	private TemporalAccessor timestamp;

	private ResponseDocumentComposition composition;

	private ChargeItem chargeItem;

	private Signature signature;

	private ResponseDocumentBundle(Bundle bundle) {
		super();
		this.bundle = bundle;

		validate();
		extractParameters();
	}

	private ResponseDocumentBundle(UUID uuid, TemporalAccessor timestamp, ResponseDocumentComposition composition,
			ChargeItem chargeItem) {
		super();
		this.uuid = uuid;
		this.timestamp = timestamp;
		this.composition = composition;
		this.chargeItem = chargeItem;

		bundle = null;
	}

	private void validate() {
		MetaHelper.assertProfile(bundle, PROFILE_URL);

		assertFixed(BundleType.DOCUMENT, () -> bundle.getType());
		assertThat(() -> bundle.getEntry().size() == 2, "exactly 2 entries needed in bundle of " + PROFILE_URL);
		assertThat(
				() -> bundle.getEntry().get(0).getResource() instanceof Composition,
				"first entry resource must be a Composition in bundle of " + PROFILE_URL);
		assertThat(
				() -> bundle.getEntry().get(1).getResource() instanceof org.hl7.fhir.r4.model.ChargeItem,
				"second entry must be a ChargeItem in bundle of " + PROFILE_URL);
	}

	private void extractParameters() {
		uuid = IdentifierHelper.getUuid(required(bundle::getIdentifier, "uuid identifier is missing"));

		var timestampElement = required(bundle::getTimestampElement, "timestamp needed in bundle");
		timestamp = DateTimeTypeConverter.convertDateTimeTypePrecision(timestampElement);

		var compositionResource = bundle.getEntry().get(0).getResource();
		composition = ResponseDocumentComposition.from((Composition) compositionResource);

		var chargeItemResource = bundle.getEntry().get(1).getResource();
		chargeItem = ChargeItem.from((org.hl7.fhir.r4.model.ChargeItem) chargeItemResource);

		var bundleSignature = bundle.getSignature();
		if (bundleSignature == null || bundleSignature.isEmpty()) {
			assertThat(() -> chargeItem.getStatus() != Status.BILLABLE,
					"chargeItem must be of status not-billable if signature is not set");
		} else {
			signature = Signature.from(bundleSignature);
			assertThat(() -> chargeItem.getStatus() == Status.BILLABLE,
					"chargeItem must be of status billable if signature is set");
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public TemporalAccessor getTimestamp() {
		return timestamp;
	}

	public ResponseDocumentComposition getComposition() {
		return composition;
	}

	public ChargeItem getChargeItem() {
		return chargeItem;
	}

	public Signature getSignature() {
		return signature;
	}

	public Bundle toBundle() {
		var result = new Bundle();
		result.setMeta(MetaHelper.withProfileUrl(PROFILE_URL));
		result.setIdentifier(IdentifierHelper.createUuidIdentifier(uuid));
		result.setType(BundleType.DOCUMENT);
		result.setTimestampElement(new InstantType(DateTimeTypeConverter.temporalAccessorToDateTimeType(timestamp)));

		var compositionEntry = new BundleEntryComponent();
		compositionEntry.setFullUrl(UuidHelper.toUuidString(UUID.randomUUID()));
		compositionEntry.setResource(composition.toComposition());
		result.addEntry(compositionEntry);

		var chargeItemEntry = new BundleEntryComponent();
		chargeItemEntry.setFullUrl(composition.getChargeItemReferenceUrl());
		chargeItemEntry.setResource(chargeItem.toChargeItem());
		result.addEntry(chargeItemEntry);

		return result;
	}

	public Bundle getOriginalBundle() {
		return bundle;
	}
}
