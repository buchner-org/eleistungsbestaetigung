package de.yoshteq.dcs.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;

@Provider
@Consumes({ "application/fhir+json", MediaType.APPLICATION_JSON, "application/fhir+xml", MediaType.APPLICATION_XML })
@Produces({ "application/fhir+json", MediaType.APPLICATION_JSON, "application/fhir+xml", MediaType.APPLICATION_XML })
public class FhirProvider implements MessageBodyReader<Resource>, MessageBodyWriter<Resource>
{
	private static final Logger log = Logger.getLogger(FhirProvider.class.getName());
	private static final FhirContext ctx = FhirContext.forR4();

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return Resource.class.isAssignableFrom(type);
	}

	@Override
	public Resource readFrom(
		Class<Resource> type,
		Type genericType,
		Annotation[] annotations,
		MediaType mediaType,
		MultivaluedMap<String, String> httpHeaders,
		InputStream entityStream)
		throws IOException,
		WebApplicationException
	{
		log.entering(this.getClass().getName(), "readFrom");
		try
		{
			EncodingEnum encoding = getEncoding(mediaType);
			IParser parser = encoding == EncodingEnum.JSON ? ctx.newJsonParser() : ctx.newXmlParser();
			return (Resource) parser.parseResource(entityStream);
		}
		catch (Exception e)
		{
			throw new IOException("an error occurred during resource deserialization", e);
		}
		finally
		{
			log.exiting(this.getClass().getName(), "readFrom");
		}
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return Resource.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(
		Resource t,
		Class<?> type,
		Type genericType,
		Annotation[] annotations,
		MediaType mediaType,
		MultivaluedMap<String, Object> httpHeaders,
		OutputStream entityStream)
		throws IOException,
		WebApplicationException
	{
		log.entering(this.getClass().getName(), "writeTo");
		try
		{
			EncodingEnum encoding = getEncoding(mediaType);
			IParser parser = encoding == EncodingEnum.JSON ? ctx.newJsonParser() : ctx.newXmlParser();
			parser.setPrettyPrint(isPretty());
			parser.encodeResourceToWriter(t, new java.io.OutputStreamWriter(entityStream));
		}
		catch (Exception e)
		{
			throw new IOException("an error occurred during resource serialization", e);
		}
		finally
		{
			log.exiting(this.getClass().getName(), "writeTo");
		}
	}

	protected boolean isPretty()
	{
		return false;
	}

	@Override
	public long getSize(Resource t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return -1;
	}

	private EncodingEnum getEncoding(MediaType mediaType)
	{
		if (mediaType != null)
		{
			if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE) ||
				mediaType.getSubtype().contains("json"))
			{
				return EncodingEnum.JSON;
			}
			else if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE) ||
				mediaType.getSubtype().contains("xml"))
			{
				return EncodingEnum.XML;
			}
		}
		return EncodingEnum.XML;
	}
}
