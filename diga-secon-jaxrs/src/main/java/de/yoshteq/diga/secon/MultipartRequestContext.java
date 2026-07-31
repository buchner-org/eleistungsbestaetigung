package de.yoshteq.diga.secon;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.core.MediaType;

import org.apache.commons.fileupload2.core.RequestContext;

public class MultipartRequestContext implements RequestContext {
	private final InputStream inputStream;
	private final MediaType mediaType;

	public MultipartRequestContext(InputStream inputStream, MediaType mediaType) {
		this.inputStream = inputStream;
		this.mediaType = mediaType;
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public String getContentType() {
		return mediaType.toString();
	}

	@Override
	public long getContentLength() {
		return -1;
	}

	@Override
	public String getCharacterEncoding() {
		return StandardCharsets.UTF_8.name();
	}

	@Override
	public boolean isMultipartRelated() {
		return false;
	}
}
