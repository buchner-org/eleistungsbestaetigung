package de.yoshteq.dcs.confirmation;

import de.yoshteq.dcs.fhir.model.ChargeItem.Status;

public enum ConfirmationRequestState
{
		PENDING(Status.UNKNOWN), 
		CONFIRMED(Status.BILLABLE), 
		DECLINED(Status.NOT_BILLABLE), 
		CANCELLED(Status.ABORTED);
	
	private Status chargeItemStatus;

	private ConfirmationRequestState(Status chargeItemStatus)
	{
		this.chargeItemStatus = chargeItemStatus;
	}

	public Status getChargeItemStatus()
	{
		return chargeItemStatus;
	}
}
