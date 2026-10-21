package de.yoshteq.dcs.secon;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import de.tk.opensource.secon.Directory;
import de.yoshteq.dcs.DcsRuntimeException;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CacheResult;

@ApplicationScoped
public class SeconDirectory implements Directory
{

	private static final String TRUSTED_CERTIFICATE_IK_CACHE = "trusted-certificate-ik-cache";

	private static final String TRUSTED_CERTIFICATE_SERIAL_CACHE = "trusted-certificate-serial-cache";

	@Inject
	EntityManager entityManager;

	@Inject
	@CacheName(TRUSTED_CERTIFICATE_SERIAL_CACHE)
	Cache serialCache;

	@Inject
	@CacheName(TRUSTED_CERTIFICATE_IK_CACHE)
	Cache ikCache;

	private CertificateFactory certificateFactory;

	@PostConstruct
	void init()
	{
		try
		{
			certificateFactory = CertificateFactory.getInstance("X.509");
		}
		catch (CertificateException e)
		{
			throw new DcsRuntimeException("Could not initialize needed crypto", e);
		}
	}

	@Transactional
	void store(TrustedCertificateEntity trustedCertificateEntity)
	{
		entityManager.merge(trustedCertificateEntity);
		ikCache.invalidate(trustedCertificateEntity.getIk()).await().indefinitely();
		serialCache.invalidate(trustedCertificateEntity.getSerialNumber()).await().indefinitely();
	}

	@Transactional
	void clearExpiredCertificates(OffsetDateTime lastUpdatedDateTime)
	{
		entityManager.createQuery("DELETE FROM TrustedCertificateEntity WHERE updateTimestamp < :lastUpdatedDateTime")
			.setParameter("lastUpdatedDateTime", lastUpdatedDateTime.minusMinutes(1))
			.executeUpdate();
	}

	@CacheResult(cacheName = TRUSTED_CERTIFICATE_IK_CACHE)
	X509Certificate getCertificateForIk(int ik) throws CertificateException
	{
		var entity = entityManager.createQuery("SELECT C FROM TrustedCertificateEntity C WHERE C.ik = :ik ORDER BY C.validFrom DESC", TrustedCertificateEntity.class)
			.setParameter("ik", ik)
			.setMaxResults(1)
			.getSingleResult();

		if (entity != null)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(entity.getData());
			return (X509Certificate) certificateFactory.generateCertificate(bais);
		}

		return null;
	}

	@CacheResult(cacheName = TRUSTED_CERTIFICATE_SERIAL_CACHE)
	X509Certificate getCertificateForSerialNumber(BigInteger serialNumber) throws CertificateException
	{
		var entity = entityManager.createQuery("SELECT C FROM TrustedCertificateEntity C WHERE C.ik IN (SELECT T.ik FROM TrustedCertificateEntity T WHERE T.serialNumber = :serialNumber) ORDER BY C.validFrom DESC", TrustedCertificateEntity.class)
			.setParameter("serialNumber", serialNumber)
			.setMaxResults(1)
			.getSingleResult();
		
		if (entity != null)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(entity.getData());
			return (X509Certificate) certificateFactory.generateCertificate(bais);
		}

		return null;
	}

	@Override
	public Optional<X509Certificate> certificate(X509CertSelector selector) throws Exception
	{
		var serialNumber = selector.getSerialNumber();
		var cert = getCertificateForSerialNumber(serialNumber);
		return Optional.ofNullable(cert);
	}

	@Override
	public Optional<X509Certificate> certificate(String identifier) throws Exception
	{
		try
		{
			var ikNumber = Integer.parseInt(identifier);
			return Optional.ofNullable(getCertificateForIk(ikNumber));
		}
		catch (NumberFormatException e)
		{
			return Optional.empty();
		}
	}

	public Optional<OffsetDateTime> getLatestUpdateTime()
	{
		var cert = entityManager.createQuery("SELECT T FROM TrustedCertificateEntity T ORDER BY T.updateTimestamp DESC", TrustedCertificateEntity.class)
			.setMaxResults(1)
			.getResultStream()
			.findFirst();

		if (cert.isPresent())
		{
			return Optional.of(cert.get().getUpdateTimestamp());
		}

		return Optional.empty();
	}
}
