package de.yoshteq.dcs.secon;

import java.math.BigInteger;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trusted_certificate")
public class TrustedCertificateEntity
{

	@Id
	@Column(name = "serial_number")
	private BigInteger serialNumber;

	@Column(name = "ik")
	private int ik;

	@Column(name = "subject")
	private String subject;

	@Column(name = "valid_from")
	private OffsetDateTime validFrom;

	@Column(name = "data")
	private byte[] data;

	@Column(name = "update_timestamp")
	private OffsetDateTime updateTimestamp;

	TrustedCertificateEntity()
	{
		super();
	}

	TrustedCertificateEntity(BigInteger serialNumber, Integer ik, String subject, OffsetDateTime validfrom,  byte[] data, OffsetDateTime updateTimestamp)
	{
		super();
		this.serialNumber = serialNumber;
		this.ik = ik;
		this.subject = subject;
		this.validFrom = validfrom;
		this.data = data;
		this.updateTimestamp = updateTimestamp;
	}

	public int getIk()
	{
		return ik;
	}

	void setIk(int ik)
	{
		this.ik = ik;
	}

	public String getSubject()
	{
		return subject;
	}

	void setSubject(String subject)
	{
		this.subject = subject;
	}

	public byte[] getData()
	{
		return data;
	}

	void setData(byte[] data)
	{
		this.data = data;
	}

	public BigInteger getSerialNumber()
	{
		return serialNumber;
	}

	void setSerialNumber(BigInteger serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	public OffsetDateTime getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	void setUpdateTimestamp(OffsetDateTime updateTimestamp)
	{
		this.updateTimestamp = updateTimestamp;
	}

	public OffsetDateTime getValidFrom()
	{
		return validFrom;
	}

	void setValidFrom(OffsetDateTime validFrom)
	{
		this.validFrom = validFrom;
	}
}
