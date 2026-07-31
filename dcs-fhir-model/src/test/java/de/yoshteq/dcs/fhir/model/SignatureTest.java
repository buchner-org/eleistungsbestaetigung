package de.yoshteq.dcs.fhir.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

import de.yoshteq.dcs.fhir.TestFileReader;
import de.yoshteq.dcs.fhir.TestStringWriter;

class SignatureTest
{

	@Test
	void testRecreateValidSignature()
	{
		Bundle bundleResource = TestFileReader.readFile("signature/ValidResponseDocumentWithSignature.xml");

		var expectedSignature = ResponseDocumentBundle.from(bundleResource).getSignature();

		var signature = Signature.create(
			expectedSignature.getSignatureDateTime(),
			expectedSignature.getBestaetigungssystemIk(),
			expectedSignature.getDetachedSignatureBytes());

		assertEquals(TestStringWriter.toString(bundleResource.getSignature()), TestStringWriter.toString(signature.toSignature()));
	}

	@Test
	void testValidSignature()
	{
		Bundle bundleResource = TestFileReader.readFile("signature/ValidResponseDocumentWithSignature.xml");

		var signature = ResponseDocumentBundle.from(bundleResource).getSignature();

		assertEquals(OffsetDateTime.parse("2022-07-07T10:42:53.333208+02:00"), signature.getSignatureDateTime());
		assertEquals(123456780, signature.getBestaetigungssystemIk());
		assertEquals("Lorem ipsum dolr sir", new String(signature.getDetachedSignatureBytes(), StandardCharsets.UTF_8));
	}

}
