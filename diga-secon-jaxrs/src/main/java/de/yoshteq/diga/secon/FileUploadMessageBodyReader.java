package de.yoshteq.diga.secon;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload2.core.AbstractFileUpload;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemFactory;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.RequestContext;

public class FileUploadMessageBodyReader<I extends FileItem<I>, F extends FileItemFactory<I>>
		extends AbstractFileUpload<MultipartRequestContext, I, F> {

	FileUploadMessageBodyReader() {
	}

	public FileUploadMessageBodyReader(F fileItemFactory) {
		setFileItemFactory(fileItemFactory);
	}

	@Override
	public FileItemInputIterator getItemIterator(MultipartRequestContext request)
			throws FileUploadException, IOException {
		return super.getItemIterator((RequestContext) request);
	}

	@Override
	public Map<String, List<I>> parseParameterMap(MultipartRequestContext request)
			throws FileUploadException {
		return parseParameterMap((RequestContext) request);
	}

	@Override
	public List<I> parseRequest(MultipartRequestContext request) throws FileUploadException {
		return parseRequest((RequestContext) request);
	}

}
