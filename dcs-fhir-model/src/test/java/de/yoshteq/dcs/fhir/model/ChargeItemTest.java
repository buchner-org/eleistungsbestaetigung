package de.yoshteq.dcs.fhir.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.TestFileReader;
import de.yoshteq.dcs.fhir.TestStringWriter;
import de.yoshteq.dcs.fhir.model.ChargeItem.Status;
import de.yoshteq.dcs.fhir.model.Performer.FunctionCode;

class ChargeItemTest {
	@Test
	void testValidChargeItemWithMidwife() {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader
				.readFile("chargeitem/ValidChargeItemWithMidwife.xml");

		var chargeItem = ChargeItem.from(chargeItemResource);

		assertEquals("11122e3e-cb4a-55b6-77f8-dc9dbdb111c2", chargeItem.getUuid().toString());
		assertEquals(Status.UNKNOWN, chargeItem.getStatus());

		assertEquals("0230", chargeItem.getCode());
		assertEquals(OffsetDateTime.parse("2022-06-12T16:50:00Z"),
				chargeItem.getOccurrence().getStartDateTime());
		assertEquals(OffsetDateTime.parse("2022-06-12T17:50:00Z"),
				chargeItem.getOccurrence().getEndDateTime());

		assertEquals("K121212345", chargeItem.getPatientKvnr());
		assertEquals(123456780, chargeItem.getKostentraegerIK());

		assertEquals(987654321, chargeItem.getPerformer().getOrganizationIK());
		assertEquals(FunctionCode.HEBAMME, chargeItem.getPerformer().getFunction());
		assertEquals("Sarah Schneider", chargeItem.getPerformer().getOrganizationDisplayValue());
		assertNull(chargeItem.getPerformer().getPractitionerDisplayValue());
	}

	@Test
	void testRecreateValidChargeItemWithMidwife() {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader
				.readFile("chargeitem/ValidChargeItemWithMidwife.xml");

		var expectedChargeItem = ChargeItem.from(chargeItemResource);

		var chargeItem = ChargeItem.create(
				expectedChargeItem.getPolicyVersion(),
				expectedChargeItem.getUuid(),
				expectedChargeItem.getStatus(),
				expectedChargeItem.getPatientKvnr(),
				expectedChargeItem.getKostentraegerIK(),
				expectedChargeItem.getCode(),
				expectedChargeItem.getOccurrence(),
				expectedChargeItem.getPerformer());

		var fhirChargeItem = chargeItem.toChargeItem();
		assertEquals(TestStringWriter.toString(chargeItemResource), TestStringWriter.toString(fhirChargeItem));
	}

	@Test
	void testValidChargeItemWithEmployedMidwife() {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader
				.readFile("chargeitem/ValidChargeItemWithEmployedMidwife.xml");

		var chargeItem = ChargeItem.from(chargeItemResource);

		assertEquals(987654321, chargeItem.getPerformer().getOrganizationIK());
		assertEquals(FunctionCode.HEBAMME, chargeItem.getPerformer().getFunction());
		assertEquals("Hebammenpraxis Müller", chargeItem.getPerformer().getOrganizationDisplayValue());
		assertEquals("Sarah Schneider", chargeItem.getPerformer().getPractitionerDisplayValue());
	}

	@Test
	void testValidChargeItemWithDateTimeOccurence() {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader
				.readFile("chargeitem/ValidChargeItemWithDateTimeOccurence.xml");

		var chargeItem = ChargeItem.from(chargeItemResource);

		assertEquals("2022-06-12", chargeItem.getOccurrence().getDateTime().toString());
	}

	@Test
	void testValidChargeItemWithoutPolicyVersion() {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader
				.readFile("chargeitem/ValidChargeItemWithoutPolicyVersion.xml");

		var chargeItem = ChargeItem.from(chargeItemResource);

		assertEquals(0, chargeItem.getPolicyVersion());
	}

	@Test
	void testValidChargeItemWithLbnr() {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader
				.readFile("chargeitem/ValidChargeItemWithLBNR.xml");

		var chargeItem = ChargeItem.from(chargeItemResource);

		assertEquals("123456789", chargeItem.getPerformer().getPractitionerIdentifierValue());
	}

	@ParameterizedTest
	@CsvSource({
			"'InvalidChargeItemMissingUuid.xml', 'uuid'",
			"'InvalidChargeItemInvalidCodeSystem.xml', https://e-lb.de/fhir/sid/KK_ELB_ChargeItemCode",
			"'InvalidChargeItemMissingPerformerOrganization.xml', Organization",
			"'InvalidChargeItemInvalidKvnr.xml', KVID",
			"'InvalidChargeItemMissingPerformerFunction.xml', 'Performer function'",
			"'InvalidChargeItemMissingPayor.xml', 'https://e-lb.de/fhir/StructureDefinition/KK_ELB_Payor'",
			"'InvalidChargeItemWrongPolicyVersion.xml', 'chargeItem.meta.security.code'",
			"'InvalidChargeItemWithoutLBNR.xml', 'chargeitem.perfomer.practitioner.actor.identifier'",
	})
	void testInvalidChargeItems(String fileName, String expectedMessagePart) {
		org.hl7.fhir.r4.model.ChargeItem chargeItemResource = TestFileReader.readFile("chargeitem/" + fileName);

		var exception = assertThrows(FhirValidationException.class, () -> {
			ChargeItem.from(chargeItemResource);
		});

		assertTrue(exception.getMessage().contains(expectedMessagePart), exception.getMessage());
	}

}
