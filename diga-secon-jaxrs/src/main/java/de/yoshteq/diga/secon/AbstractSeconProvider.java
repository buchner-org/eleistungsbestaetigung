package de.yoshteq.diga.secon;

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import de.tk.opensource.secon.Directory;
import de.tk.opensource.secon.Identity;
import de.tk.opensource.secon.SECON;
import de.tk.opensource.secon.Subscriber;

public abstract class AbstractSeconProvider
{

	protected static final String SECON_PARAMETER_NUTZDATEN = "nutzdaten";

	protected static final String SECON_PARAMETER_VERFAHREN = "verfahren";

	protected static final String SECON_PARAMETER_IKEMPFAENGER = "ikempfaenger";

	protected static final String SECON_PARAMETER_IKSENDER = "iksender";

	protected static final List<String> KNOWN_PARAMETERS = Arrays.asList(
		SECON_PARAMETER_NUTZDATEN,
		SECON_PARAMETER_VERFAHREN,
		SECON_PARAMETER_IKEMPFAENGER,
		SECON_PARAMETER_IKSENDER);

	@Context
	Providers providers;

	@Inject
	Identity seconIdentity;

	@Inject
	Directory seconDirectory;

	@Inject
	@ConfigProperty(name = "jaxrs-secon.default-inner-mime-type", defaultValue = MediaType.APPLICATION_XML)
	String defaultInnerMimeType;

	protected Subscriber seconSubscriber;

	protected String seconIK;

	@PostConstruct
	void initProvider()
	{
		seconSubscriber = SECON.subscriber(seconIdentity, seconDirectory);
		try
		{
			seconIK = X509CertificateUtil.extractIkAsString(seconIdentity.certificate()).orElseThrow();
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Could not initialize SeconProvider", e);
		}
	}

	protected MediaType getInnerMediaType(MediaType mediaType)
	{
		return MediaType.valueOf(mediaType.getParameters().getOrDefault(SeconMediaType.INNER_TYPE_PARAM, defaultInnerMimeType));
	}

}