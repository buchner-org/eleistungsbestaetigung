package de.yoshteq.dcs.performer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "performer_organization")
public class PerformerOrganization
{

	@Id
	@Column(name = "ik")
	private Integer ik;

	@Column(name = "institution_name_row_1")
	private String institutionNameRow1;

	@Column(name = "institution_name_row_2")
	private String institutionNameRow2;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	PerformerOrganization()
	{
	}

	public Integer getIk()
	{
		return ik;
	}

	void setIk(Integer ik)
	{
		this.ik = ik;
	}

	public String getInstitutionNameRow1()
	{
		return institutionNameRow1;
	}

	void setInstitutionNameRow1(String institutionNameRow1)
	{
		this.institutionNameRow1 = institutionNameRow1;
	}

	public String getInstitutionNameRow2()
	{
		return institutionNameRow2;
	}

	void setInstitutionNameRow2(String institutionNameRow2)
	{
		this.institutionNameRow2 = institutionNameRow2;
	}

	public String getFirstName()
	{
		return firstName;
	}

	void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

}
