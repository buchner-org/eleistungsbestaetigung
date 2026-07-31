package de.yoshteq.diga.secon;

import jakarta.ws.rs.core.MediaType;

public class SeconMediaType
{

	static final String INNER_TYPE_PARAM = "innerType";

	public static final String INNER_XML = MediaType.MULTIPART_FORM_DATA + ";" + INNER_TYPE_PARAM + "=\"" + MediaType.APPLICATION_XML + "\"";

	public static final String INNER_JSON = MediaType.MULTIPART_FORM_DATA + ";" + INNER_TYPE_PARAM + "=\"" + MediaType.APPLICATION_JSON + "\"";

	private SeconMediaType()
	{
	}

}
