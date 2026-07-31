package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.hl7.fhir.r4.model.Patient;

@DisplayName("MetaHelper Tests")
class MetaHelperTest
{

	@Test
	@DisplayName("should create meta with profile URL")
	void withProfileUrl()
	{
		var profileUrl = "http://example.com/profile/Patient";

		var result = MetaHelper.withProfileUrl(profileUrl);

		assertNotNull(result);
		assertEquals(1, result.getProfile().size());
		assertEquals(profileUrl, result.getProfile().get(0).getValue());
	}

	@Test
	@DisplayName("should assert resource has correct profile")
	void assertProfile()
	{
		var profileUrl = "http://example.com/profile/Patient";
		var patient = new Patient();
		patient.getMeta().addProfile(profileUrl);

		assertDoesNotThrow(() -> MetaHelper.assertProfile(patient, profileUrl));
	}

	@Test
	@DisplayName("should throw exception when resource has wrong profile")
	void assertProfileWrong()
	{
		var expectedProfile = "http://example.com/profile/Patient";
		var actualProfile = "http://example.com/profile/Other";
		var patient = new Patient();
		patient.getMeta().addProfile(actualProfile);

		assertThrows(
			FhirValidationException.class,
			() -> MetaHelper.assertProfile(patient, expectedProfile)
		);
	}

	@Test
	@DisplayName("should throw exception when resource has no profile")
	void assertProfileMissing()
	{
		var patient = new Patient();

		assertThrows(
			Exception.class,
			() -> MetaHelper.assertProfile(patient, "http://example.com/profile/Patient")
		);
	}

	@Test
	@DisplayName("should create multiple meta instances with different URLs")
	void multipleMetaWithDifferentUrls()
	{
		var url1 = "http://example.com/profile/Patient1";
		var url2 = "http://example.com/profile/Patient2";

		var meta1 = MetaHelper.withProfileUrl(url1);
		var meta2 = MetaHelper.withProfileUrl(url2);

		assertEquals(url1, meta1.getProfile().get(0).getValue());
		assertEquals(url2, meta2.getProfile().get(0).getValue());
		assertNotEquals(meta1.getProfile().get(0).getValue(), meta2.getProfile().get(0).getValue());
	}

}
