package de.yoshteq.dcs.fhir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("FhirBinaryFileHelper Tests")
class FhirBinaryFileHelperTest
{

	@Test
	@DisplayName("should create binary from text file with detected content type")
	void createBinaryFromFileTextDetected(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("test.txt");
		var content = "test content";
		Files.write(path, content.getBytes());

		var result = FhirBinaryFileHelper.createBinaryFromFile(path);

		assertNotNull(result);
		assertEquals("text/plain", result.getContentType());
		var expectedData = Base64.getEncoder().encode(content.getBytes());
		assertArrayEquals(expectedData, result.getData());
	}

	@Test
	@DisplayName("should create binary from file with explicit content type")
	void createBinaryFromFileExplicitContentType(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("test.bin");
		var content = new byte[] { 1, 2, 3, 4, 5 };
		Files.write(path, content);

		var result = FhirBinaryFileHelper.createBinaryFromFile(path, "application/octet-stream");

		assertNotNull(result);
		assertEquals("application/octet-stream", result.getContentType());
		var expectedData = Base64.getEncoder().encode(content);
		assertArrayEquals(expectedData, result.getData());
	}

	@Test
	@DisplayName("should create binary from JSON file")
	void createBinaryFromJsonFile(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("data.json");
		var content = "{\"key\": \"value\"}";
		Files.write(path, content.getBytes());

		var result = FhirBinaryFileHelper.createBinaryFromFile(path);

		assertNotNull(result);
		assertEquals("application/json", result.getContentType());
	}

	@Test
	@DisplayName("should create binary from XML file")
	void createBinaryFromXmlFile(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("data.xml");
		var content = "<?xml version=\"1.0\"?><root/>";
		Files.write(path, content.getBytes());

		var result = FhirBinaryFileHelper.createBinaryFromFile(path);

		assertNotNull(result);
		assertEquals("application/xml", result.getContentType());
	}

	@Test
	@DisplayName("should create Base64 binary from file content")
	void createBase64BinaryFromFile(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("test.txt");
		var content = "test content";
		Files.write(path, content.getBytes());

		var result = FhirBinaryFileHelper.createBase64BinaryFromFile(path);

		assertNotNull(result);
		var expectedData = Base64.getEncoder().encode(content.getBytes());
		assertArrayEquals(expectedData, result);
	}

	@Test
	@DisplayName("should encode binary data as Base64")
	void createBase64BinaryEncoding(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("test.bin");
		var content = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD };
		Files.write(path, content);

		var result = FhirBinaryFileHelper.createBase64BinaryFromFile(path);

		var decoded = Base64.getDecoder().decode(result);
		assertArrayEquals(content, decoded);
	}

	@Test
	@DisplayName("should throw exception for non-existent file")
	void createBinaryFromNonExistentFile()
	{
		var path = Path.of("/non/existent/file.txt");

		assertThrows(
			IOException.class,
			() -> FhirBinaryFileHelper.createBinaryFromFile(path)
		);
	}

	@Test
	@DisplayName("should throw exception for non-existent file with explicit content type")
	void createBinaryFromNonExistentFileWithContentType()
	{
		var path = Path.of("/non/existent/file.txt");

		assertThrows(
			IOException.class,
			() -> FhirBinaryFileHelper.createBinaryFromFile(path, "text/plain")
		);
	}

	@Test
	@DisplayName("should handle empty file")
	void createBinaryFromEmptyFile(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("empty.txt");
		Files.write(path, new byte[0]);

		var result = FhirBinaryFileHelper.createBinaryFromFile(path);

		assertNotNull(result);
		assertEquals(0, result.getData().length);
	}

	@Test
	@DisplayName("should handle large file")
	void createBinaryFromLargeFile(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("large.bin");
		var content = new byte[10000];
		for (int i = 0; i < content.length; i++)
		{
			content[i] = (byte) (i % 256);
		}
		Files.write(path, content);

		var result = FhirBinaryFileHelper.createBinaryFromFile(path, "application/octet-stream");

		assertNotNull(result);
		var decoded = Base64.getDecoder().decode(result.getData());
		assertArrayEquals(content, decoded);
	}

	@Test
	@DisplayName("should handle binary file with specific byte patterns")
	void createBinaryFromBinaryFile(@TempDir Path tempDir) throws IOException
	{
		var path = tempDir.resolve("binary.bin");
		var content = new byte[] {
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF
		};
		Files.write(path, content);

		var result = FhirBinaryFileHelper.createBinaryFromFile(path, "application/octet-stream");

		var decoded = Base64.getDecoder().decode(result.getData());
		assertArrayEquals(content, decoded);
	}

}
