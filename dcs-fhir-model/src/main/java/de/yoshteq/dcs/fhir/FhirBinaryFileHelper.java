package de.yoshteq.dcs.fhir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.hl7.fhir.r4.model.Binary;

public class FhirBinaryFileHelper
{

	private FhirBinaryFileHelper()
	{

	}

	public static Binary createBinaryFromFile(Path path) throws IOException
	{
		var contentType = Files.probeContentType(path);
		return createBinaryFromFile(path, contentType);
	}

	public static Binary createBinaryFromFile(Path path, String contentType) throws IOException
	{
		var binary = new Binary();
		binary.setContentType(contentType);
		binary.setData(createBase64BinaryFromFile(path));
		return binary;
	}

	public static byte[] createBase64BinaryFromFile(Path path) throws IOException
	{
		return Base64.getEncoder().encode(Files.readAllBytes(path));
	}

}
