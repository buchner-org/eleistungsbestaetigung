package de.yoshteq.dcs.fhir.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.TestFileReader;
import de.yoshteq.dcs.fhir.model.ChargeItem.Status;

class ResponseDocumentBundleTest
{

	@Test
	void testValidCompositionWithSignature()
	{
		Bundle bundleResource = TestFileReader.readFile("response_document/ValidResponseDocumentWithSignature.xml");

		var bundle = ResponseDocumentBundle.from(bundleResource);

		assertEquals("4444d555-666e-77a8-999c-222222b2d33c", bundle.getUuid().toString());
		assertEquals(ZonedDateTime.parse("2022-06-20T08:30:00Z"), bundle.getTimestamp());

		assertEquals(Status.BILLABLE, bundle.getChargeItem().getStatus());
		assertNotNull(bundle.getSignature());
	}

	@Test
	void testValidCompositionWithoutSignature()
	{
		Bundle bundleResource = TestFileReader.readFile("response_document/ValidResponseDocumentWithoutSignature.xml");

		var bundle = ResponseDocumentBundle.from(bundleResource);

		assertEquals(Status.NOT_BILLABLE, bundle.getChargeItem().getStatus());
		assertNull(bundle.getSignature());
	}

	@Test
	void testInvalidCompositionBillableWithoutSignature()
	{
		Bundle bundleResource = TestFileReader.readFile("response_document/InvalidResponseDocumentBillableWithoutSignature.xml");

		var exception = assertThrows(FhirValidationException.class, () -> ResponseDocumentBundle.from(bundleResource));

		assertTrue(exception.getMessage().contains("chargeItem must be of status not-billable if signature is not set"), exception.getMessage());
	}
	
	@Test
	void testInvalidCompositionNotBillableWithSignature()
	{
		Bundle bundleResource = TestFileReader.readFile("response_document/InvalidResponseDocumentNotBillableWithSignature.xml");

		var exception = assertThrows(FhirValidationException.class, () -> ResponseDocumentBundle.from(bundleResource));

		assertTrue(exception.getMessage().contains("chargeItem must be of status billable if signature is set"), exception.getMessage());
	}

}
