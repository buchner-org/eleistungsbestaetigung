package de.yoshteq.dcs.push;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import de.yoshteq.dcs.insurant.Insurant;

@Entity
@Table(name = "insurant_push_task")
public class InsurantPushTask
{
	
	public static InsurantPushTask create(Long insurantId, OffsetDateTime lastRequestDateTime) {
		var task = new InsurantPushTask();
		task.insurantId = insurantId;
		task.requestDateTime = lastRequestDateTime;
		return task;
	}

	@Id
	@Column(name = "insurant_id")
	private Long insurantId;
	
	@JoinColumn(name = "insurant_id", updatable = false, insertable = false, nullable = true)
	@ManyToOne(optional = true, fetch = FetchType.EAGER, targetEntity = Insurant.class)
	private Insurant insurant;
	
	@Column(name = "request_date_time")
	private OffsetDateTime requestDateTime;
	
	@Column(name = "push_date_time")
	private OffsetDateTime pushDateTime;


	public Long getInsurantId()
	{
		return insurantId;
	}

	public void setInsurantId(Long insurantId)
	{
		this.insurantId = insurantId;
	}

	public OffsetDateTime getRequestDateTime()
	{
		return requestDateTime;
	}

	public void setRequestDateTime(OffsetDateTime lastRequestDateTime)
	{
		this.requestDateTime = lastRequestDateTime;
	}

	public OffsetDateTime getPushDateTime()
	{
		return pushDateTime;
	}

	public void setPushDateTime(OffsetDateTime pushDateTime)
	{
		this.pushDateTime = pushDateTime;
	}

	public Insurant getInsurant()
	{
		return insurant;
	}

}
