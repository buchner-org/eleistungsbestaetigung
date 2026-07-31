package de.yoshteq.diga.secon;

import java.io.InputStream;

import jakarta.ws.rs.core.MediaType;

public record SeconMultipartRequest(InputStream inputStream, MediaType mediaType) {

}
