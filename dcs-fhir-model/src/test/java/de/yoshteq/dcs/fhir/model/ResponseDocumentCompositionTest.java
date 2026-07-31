package de.yoshteq.dcs.fhir.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;

import org.hl7.fhir.r4.model.Composition;
import org.junit.jupiter.api.Test;

import de.yoshteq.dcs.fhir.FhirValidationException;
import de.yoshteq.dcs.fhir.TestFileReader;
import de.yoshteq.dcs.fhir.TestStringWriter;

class ResponseDocumentCompositionTest
{

	@Test
	void testValidComposition()
	{
		Composition compositionResource = TestFileReader.readFile("response_document/ValidResponseDocumentComposition.xml");

		var composition = ResponseDocumentComposition.from(compositionResource);

		assertTrue(composition.getChargeItemReferenceUrl().startsWith("urn:uuid:"));
		assertEquals(123456780, composition.getBestaetigungssystemIk());
		assertEquals(ZonedDateTime.parse("2022-06-19T08:00:00Z"), composition.getDate());
	}

	@Test
	void testRecreateValidComposition()
	{
		Composition compositionResource = TestFileReader.readFile("response_document/ValidResponseDocumentComposition.xml");

		var expectedComposition = ResponseDocumentComposition.from(compositionResource);

		var composition = ResponseDocumentComposition
			.create(expectedComposition.getBestaetigungssystemIk(), expectedComposition.getDate(), expectedComposition.getChargeItemReferenceUrl());

		assertEquals(TestStringWriter.toString(compositionResource), TestStringWriter.toString(composition.toComposition()));
	}

	@Test
	void testInvalidCompositionNoSection()
	{
		Composition compositionResource = TestFileReader.readFile("response_document/InvalidResponseDocumentCompositionNoSection.xml");

		var exception = assertThrows(FhirValidationException.class, () -> ResponseDocumentComposition.from(compositionResource));
		assertTrue(exception.getMessage().contains("section"), exception.getMessage());
	}

}
