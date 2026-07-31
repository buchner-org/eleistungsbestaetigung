package de.yoshteq.diga.secon;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class SeconContainer<R> {

	private final SeconRequestParameters requestParameters;

	private final R entity;

	private final X509Certificate receiverCertificate;

	public X509Certificate getReceiverCertificate() {
		return receiverCertificate;
	}

	public static <R> SeconContainer<R> create(String ikEmpfaenger, R entity) {
		return new SeconContainer<>(null, null, null, ikEmpfaenger, null, entity, null);
	}

	public static <R> SeconContainer<R> create(String ikEmpfaenger, X509Certificate receiverCertificate, R entity) {
		return new SeconContainer<>(null, null, receiverCertificate, ikEmpfaenger, null, entity, null);
	}

	public static <R> SeconContainer<R> create(String ikEmpfaenger, String verfahren, R entity) {
		return new SeconContainer<>(null, null, null, ikEmpfaenger, verfahren, entity, null);
	}

	SeconContainer(
			String ikSender,
			X509Certificate senderCertificate,
			X509Certificate receiverCertificate,
			String ikEmpfaenger,
			String verfahren,
			R entity,
			Map<String, String> additionalParameters) {
		this.requestParameters = new SeconRequestParameters(ikSender, senderCertificate, ikEmpfaenger, verfahren,
				additionalParameters);
		this.entity = entity;
		this.receiverCertificate = receiverCertificate;
	}

	public <T> SeconContainer<T> respondWith(T entity) {
		return new SeconContainer<>(null, null, getSenderCertificate(), getIkSender(), getVerfahren().orElse(null), entity, null);
	}

	public String getIkSender() {
		return requestParameters.getIkSender();
	}

	public String getIkEmpfaenger() {
		return requestParameters.getIkEmpfaenger();
	}

	public Optional<String> getVerfahren() {
		return Optional.ofNullable(requestParameters.getVerfahren());
	}

	public Map<String, String> getAdditionalParameters() {
		return Collections.unmodifiableMap(requestParameters.getAdditionalParamters());
	}

	public R getEntity() {
		return entity;
	}

	public X509Certificate getSenderCertificate() {
		return requestParameters.getSenderCertificate();
	}
}
