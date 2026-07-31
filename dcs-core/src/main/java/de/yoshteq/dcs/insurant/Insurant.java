package de.yoshteq.dcs.insurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import de.yoshteq.dcs.MapConverter;

@Entity
@Table(name = "insurant")
@NamedQuery(name = Insurant.QUERY_BY_PSEUDONYM, query = "SELECT I FROM Insurant I WHERE I.pseudonym = :pseudonym")
@NamedQuery(name = Insurant.QUERY_BY_USER_IDENTIFIER, query = "SELECT I FROM Insurant I WHERE I.userIdentifier = :userIdentifier")
public class Insurant
{
	static final String QUERY_BY_PSEUDONYM = "insurantByPseudonym";

	static final String QUERY_BY_USER_IDENTIFIER = "insurantByUserIdentifier";

	public static Insurant with(String pseudonym, String userIdentifier)
	{
		var insurant = new Insurant();
		insurant.pseudonym = pseudonym;
		insurant.userIdentifier = userIdentifier;

		return insurant;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "pseudonym")
	private String pseudonym;

	@Column(name = "user_identifier")
	private String userIdentifier;

	@Convert(converter = MapConverter.class)
	@Column(name = "metadata")
	private Map<String, Object> metadata;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "insurant", cascade = CascadeType.ALL)
	private List<InsurantRegistration> registrations = new ArrayList<>();

	protected Insurant()
	{
		// needed for JPA
	}

	public Long getId()
	{
		return id;
	}

	void setId(Long id)
	{
		this.id = id;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	void setPseudonym(String insurantNumber)
	{
		pseudonym = insurantNumber;
	}

	public Map<String, Object> getMetadata()
	{
		if (metadata == null)
		{
			metadata = new HashMap<>();
		}
		return metadata;
	}

	void setMetadata(HashMap<String, Object> metadata)
	{
		this.metadata = metadata;
	}

	public void addMetaData(String key, Object value)
	{
		getMetadata().put(key, value);
	}

	public String getUserIdentifier()
	{
		return userIdentifier;
	}

	public void setUserIdentifier(String userIdentifier)
	{
		this.userIdentifier = userIdentifier;
	}

	public InsurantRegistration createRegistration()
	{
		var registration = InsurantRegistration.with(this);
		registrations.add(registration);

		return registration;
	}

}
