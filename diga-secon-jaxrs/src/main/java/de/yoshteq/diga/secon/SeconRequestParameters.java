package de.yoshteq.diga.secon;

import java.security.cert.X509Certificate;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;

@RequestScoped
public class SeconRequestParameters
{

	@NotBlank(message = "Der Parameter iksender muss gefüllt sein")
	private String ikSender;

	private X509Certificate senderCertificate;

	@NotBlank(message = "Der Parameter ikempfaenger muss gefüllt sein")
	private String ikEmpfaenger;

	@NotBlank(message = "Der Parameter verfahren muss gefüllt sein")
	private String verfahren;

	private Map<String, String> additionalParamters;

	SeconRequestParameters()
	{
		super();
	}

	SeconRequestParameters(
		String ikSender,
		X509Certificate senderCertificate,
		String ikEmpfaenger,
		String verfahren,
		Map<String, String> additionalParamters)
	{
		super();
		this.ikSender = ikSender;
		this.senderCertificate = senderCertificate;
		this.ikEmpfaenger = ikEmpfaenger;
		this.verfahren = verfahren;
		this.additionalParamters = additionalParamters;
	}

	public String getIkSender()
	{
		return ikSender;
	}

	void setIkSender(String ikSender)
	{
		this.ikSender = ikSender;
	}

	public String getIkEmpfaenger()
	{
		return ikEmpfaenger;
	}

	void setIkEmpfaenger(String ikEmpfaenger)
	{
		this.ikEmpfaenger = ikEmpfaenger;
	}

	public String getVerfahren()
	{
		return verfahren;
	}

	void setVerfahren(String verfahren)
	{
		this.verfahren = verfahren;
	}

	public Map<String, String> getAdditionalParamters()
	{
		return additionalParamters;
	}

	void setAdditionalParamters(Map<String, String> additionalParamters)
	{
		this.additionalParamters = additionalParamters;
	}

	void validate(Validator validator)
	{
		var violations = validator.validate(this);
		if (!violations.isEmpty())
		{
			throw new ConstraintViolationException(violations);
		}
	}

	public X509Certificate getSenderCertificate()
	{
		return senderCertificate;
	}

	void setSenderCertificate(X509Certificate senderCertificate)
	{
		this.senderCertificate = senderCertificate;
	}
}
