package de.yoshteq.dcs.push.barmer;

public class PushNotificationRequest
{

	String timestamp;

	String notificationPayloadTitle;

	String notificationPayloadText;

	String deeplinkData;

	String reminderType;

	String technicalReminderType;

	String partnerId;

	public PushNotificationRequest(
		String timestamp,
		String notificationPayloadTitle,
		String notificationPayloadText,
		String deeplinkData,
		String reminderType,
		String technicalReminderType,
		String partnerId)
	{
		super();
		this.timestamp = timestamp;
		this.notificationPayloadTitle = notificationPayloadTitle;
		this.notificationPayloadText = notificationPayloadText;
		this.deeplinkData = deeplinkData;
		this.reminderType = reminderType;
		this.technicalReminderType = technicalReminderType;
		this.partnerId = partnerId;
	}

	public String getTimestamp()
	{
		return timestamp;
	}

	public String getNotificationPayloadTitle()
	{
		return notificationPayloadTitle;
	}

	public String getNotificationPayloadText()
	{
		return notificationPayloadText;
	}

	public String getDeeplinkData()
	{
		return deeplinkData;
	}

	public String getReminderType()
	{
		return reminderType;
	}

	public String getTechnicalReminderType()
	{
		return technicalReminderType;
	}

	public String getPartnerId()
	{
		return partnerId;
	}

}
