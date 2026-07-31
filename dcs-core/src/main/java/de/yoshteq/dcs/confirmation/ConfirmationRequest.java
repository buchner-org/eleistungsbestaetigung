package de.yoshteq.dcs.confirmation;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import de.yoshteq.dcs.EncryptedValueConverter;
import de.yoshteq.dcs.FhirTemporalAccessorConverter;
import de.yoshteq.dcs.fhir.api.ConfirmationRequests;
import de.yoshteq.dcs.fhir.api.ConfirmationRequests.RequestType;
import de.yoshteq.dcs.fhir.model.ChargeItem;
import de.yoshteq.dcs.fhir.model.Occurrence;
import de.yoshteq.dcs.fhir.model.Performer;
import de.yoshteq.dcs.fhir.model.Performer.FunctionCode;
import de.yoshteq.dcs.insurant.Insurant;
import de.yoshteq.dcs.performer.PerformerOrganization;

@Entity
@Table(name = "confirmation_request")
public class ConfirmationRequest
{

	public static ConfirmationRequest with(Integer senderIk, ConfirmationRequests requests, Insurant insurant, ChargeItem chargeItem)
	{
		var confirmationRequest = new ConfirmationRequest();

		confirmationRequest.insurant = insurant;
		confirmationRequest.kvnr = chargeItem.getPatientKvnr();
		confirmationRequest.kostentraegerIK = chargeItem.getKostentraegerIK();
		confirmationRequest.state = ConfirmationRequestState.PENDING;
		confirmationRequest.senderIK = senderIk;
		confirmationRequest.requestDateTime = OffsetDateTime.now();

		confirmationRequest.responseType = requests.getRequestType() == RequestType.REQUEST_WITH_CALLBACK
			? ConfirmationResponseType.CALLBACK
			: ConfirmationResponseType.POLLING;
		confirmationRequest.softwareIdentifier = requests.getSoftwareIdentifier();
		confirmationRequest.contactMailAddress = requests.getContactMailAddress();

		if (confirmationRequest.responseType == ConfirmationResponseType.CALLBACK)
		{
			confirmationRequest.callbackUrl = requests.getCallbackUrl().toExternalForm();
		}

		confirmationRequest.uuid = chargeItem.getUuid().toString();
		confirmationRequest.serviceType = ServiceType.from(chargeItem.getPerformer().getFunction().getValue());
		confirmationRequest.code = chargeItem.getCode();
		confirmationRequest.occurrenceDateTime = chargeItem.getOccurrence().getDateTime();
		confirmationRequest.occurrenceStartDateTime = chargeItem.getOccurrence().getStartDateTime();
		confirmationRequest.occurrenceEndDateTime = chargeItem.getOccurrence().getEndDateTime();

		confirmationRequest.performerOrganizationIK = chargeItem.getPerformer().getOrganizationIK();
		confirmationRequest.performerOrganizationDisplay = chargeItem.getPerformer().getOrganizationDisplayValue();

		confirmationRequest.performerPractitionerIdentifier = chargeItem.getPerformer().getPractitionerIdentifierValue();
		confirmationRequest.performerPractitionerDisplay = chargeItem.getPerformer().getPractitionerDisplayValue();

		confirmationRequest.policyVersion = Optional.of(chargeItem.getPolicyVersion()).orElse(1);

		return confirmationRequest;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "insurant_id")
	private Insurant insurant;

	@Column(name = "kostentraeger_ik")
	private Integer kostentraegerIK;

	@Convert(converter = EncryptedValueConverter.class)
	@Column(name = "encrypted_kvnr")
	private String kvnr;

	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private ConfirmationRequestState state;

	@Column(name = "sender_ik")
	private Integer senderIK;

	@Column(name = "request_date_time")
	private OffsetDateTime requestDateTime;

	@Column(name = "response_type")
	@Enumerated(EnumType.STRING)
	private ConfirmationResponseType responseType;

	@Column(name = "software_identifier")
	private String softwareIdentifier;

	@Column(name = "contact_mail_address")
	private String contactMailAddress;

	@Column(name = "callback_url")
	private String callbackUrl;

	@Column(name = "policy_version")
	private Integer policyVersion;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "service_type")
	@Enumerated(EnumType.STRING)
	private ServiceType serviceType;

	@Column(name = "code")
	private String code;

	@Convert(converter = FhirTemporalAccessorConverter.class)
	@Column(name = "occurrence_date_time")
	private TemporalAccessor occurrenceDateTime;

	@Convert(converter = FhirTemporalAccessorConverter.class)
	@Column(name = "occurrence_start_date_time")
	private TemporalAccessor occurrenceStartDateTime;

	@Convert(converter = FhirTemporalAccessorConverter.class)
	@Column(name = "occurrence_end_date_time")
	private TemporalAccessor occurrenceEndDateTime;

	@Column(name = "performer_organization_ik")
	private Integer performerOrganizationIK;

	@Column(name = "performer_organization_display")
	private String performerOrganizationDisplay;

	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "performer_organization_ik", updatable = false, insertable = false, nullable = true)
	@ManyToOne(optional = true, fetch = FetchType.EAGER, targetEntity = PerformerOrganization.class)
	private PerformerOrganization performerOrganization;

	@Column(name = "performer_practitioner_identifier")
	private String performerPractitionerIdentifier;

	@Column(name = "performer_practitioner_display")
	private String performerPractitionerDisplay;

	@Column(name = "response_date_time")
	private OffsetDateTime responseDateTime;

	@Column(name = "processing_fail_count")
	private int processingFailCount;

	@Column(name = "response_document_bundle_uuid")
	private String responseDocumentBundleUuid;

	@Lob
	@Column(name = "response_document_bundle_xml")
	private String responseDocumentBundleXml;

	@Column(name = "callback_fail_count")
	private int callbackFailCount;

	public Long getId()
	{
		return id;
	}

	public Insurant getInsurant()
	{
		return insurant;
	}

	public ConfirmationRequestState getState()
	{
		return state;
	}

	public Integer getSenderIK()
	{
		return senderIK;
	}

	public OffsetDateTime getRequestDateTime()
	{
		return requestDateTime;
	}

	public ConfirmationResponseType getResponseType()
	{
		return responseType;
	}

	public String getSoftwareIdentifier()
	{
		return softwareIdentifier;
	}

	public String getContactMailAddress()
	{
		return contactMailAddress;
	}

	public String getCallbackUrl()
	{
		return callbackUrl;
	}

	public Integer getKostentraegerIK()
	{
		return kostentraegerIK;
	}

	public Integer getPolicyVersion()
	{
		return policyVersion;
	}

	public String getUuid()
	{
		return uuid;
	}

	public ServiceType getServiceType()
	{
		return serviceType;
	}

	public String getCode()
	{
		return code;
	}

	public TemporalAccessor getOccurrenceDateTime()
	{
		return occurrenceDateTime;
	}

	public TemporalAccessor getOccurrenceStartDateTime()
	{
		return occurrenceStartDateTime;
	}

	public TemporalAccessor getOccurrenceEndDateTime()
	{
		return occurrenceEndDateTime;
	}

	public OffsetDateTime getResponseDateTime()
	{
		return responseDateTime;
	}

	public String getResponseDocumentBundleXml()
	{
		return responseDocumentBundleXml;
	}

	public boolean isPending()
	{
		return state == ConfirmationRequestState.PENDING;
	}

	public void setResponse(boolean confirmed)
	{
		if (confirmed)
		{
			state = ConfirmationRequestState.CONFIRMED;
		}
		else
		{
			state = ConfirmationRequestState.DECLINED;
		}

		responseDateTime = OffsetDateTime.now();
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setInsurant(Insurant insurant)
	{
		this.insurant = insurant;
	}

	public void setState(ConfirmationRequestState state)
	{
		this.state = state;
	}

	public void setSenderIK(Integer senderIK)
	{
		this.senderIK = senderIK;
	}

	public void setRequestDateTime(OffsetDateTime confirmationRequestDateTime)
	{
		this.requestDateTime = confirmationRequestDateTime;
	}

	public void setResponseType(ConfirmationResponseType responseType)
	{
		this.responseType = responseType;
	}

	public void setSoftwareIdentifier(String software)
	{
		this.softwareIdentifier = software;
	}

	public void setContactMailAddress(String contactEmail)
	{
		this.contactMailAddress = contactEmail;
	}

	public void setCallbackUrl(String endpointUrl)
	{
		this.callbackUrl = endpointUrl;
	}

	public void setPolicyVersion(Integer policyVersion)
	{
		this.policyVersion = policyVersion;
	}

	public void setUuid(String identifier)
	{
		this.uuid = identifier;
	}

	public void setServiceType(ServiceType serviceType)
	{
		this.serviceType = serviceType;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public void setResponseDateTime(OffsetDateTime responseDateTime)
	{
		this.responseDateTime = responseDateTime;
	}

	public void setResponseDocumentBundleXml(String responseDocumentBundle)
	{
		this.responseDocumentBundleXml = responseDocumentBundle;
	}

	public String getKvnr()
	{
		return kvnr;
	}

	public void setKvnr(String encryptedEKVNR)
	{
		this.kvnr = encryptedEKVNR;
	}

	public Integer getPerformerOrganizationIK()
	{
		return performerOrganizationIK;
	}

	public void setPerformerOrganizationIK(Integer performerOrganizationIK)
	{
		this.performerOrganizationIK = performerOrganizationIK;
	}

	public String getPerformerOrganizationDisplay()
	{
		return performerOrganizationDisplay;
	}

	public void setPerformerOrganizationDisplay(String performerOrganizationDisplay)
	{
		this.performerOrganizationDisplay = performerOrganizationDisplay;
	}

	public PerformerOrganization getPerformerOrganization()
	{
		return performerOrganization;
	}

	public void setPerformerOrganization(PerformerOrganization performerOrganization)
	{
		this.performerOrganization = performerOrganization;
	}

	public String getPerformerPractitionerDisplay()
	{
		return performerPractitionerDisplay;
	}

	public void setPerformerPractitionerDisplay(String performerPractitionerDisplay)
	{
		this.performerPractitionerDisplay = performerPractitionerDisplay;
	}

	public void setOccurrenceDateTime(TemporalAccessor occurenceDateTime)
	{
		this.occurrenceDateTime = occurenceDateTime;
	}

	public void setOccurrenceStartDateTime(TemporalAccessor occurenceStartDateTime)
	{
		this.occurrenceStartDateTime = occurenceStartDateTime;
	}

	public void setOccurrenceEndDateTime(TemporalAccessor occurenceEndDateTime)
	{
		this.occurrenceEndDateTime = occurenceEndDateTime;
	}

	public boolean isConfirmed()
	{
		return state == ConfirmationRequestState.CONFIRMED;
	}

	public ChargeItem toChargeItem()
	{
		var status = state.getChargeItemStatus();
		var occurrence = this.occurrenceDateTime != null
			? Occurrence.create(occurrenceDateTime)
			: Occurrence.create(this.occurrenceStartDateTime, this.occurrenceEndDateTime);

		var performer = this.performerPractitionerDisplay == null
			? Performer.organization(FunctionCode.of(serviceType.getCode()), performerOrganizationIK, performerOrganizationDisplay)
			: Performer.organizationWithPractitioner(
				FunctionCode.of(serviceType.getCode()),
				performerOrganizationIK,
				performerOrganizationDisplay,
				performerPractitionerIdentifier,
				performerPractitionerDisplay);

		return ChargeItem
			.create(
				policyVersion,
				UUID.fromString(getUuid()),
				status,
				getKvnr(),
				getKostentraegerIK(),
				code,
				occurrence,
				performer);
	}

	public void setKostentraegerIK(Integer kostentraegerIK)
	{
		this.kostentraegerIK = kostentraegerIK;
	}

	public int getProcessingFailCount()
	{
		return processingFailCount;
	}

	public void incrementProcessFailCount()
	{
		this.processingFailCount += 1;
	}

	public void setProcessingFailCount(int processFailCount)
	{
		this.processingFailCount = processFailCount;
	}

	public String getResponseDocumentBundleUuid()
	{
		return responseDocumentBundleUuid;
	}

	public void setResponseDocumentBundleUuid(String responseDocumentBundleUuid)
	{
		this.responseDocumentBundleUuid = responseDocumentBundleUuid;
	}

	public int getCallbackFailCount()
	{
		return callbackFailCount;
	}

	public void incrementCallbackFailCount()
	{
		this.callbackFailCount += 1;
	}

	void setCallbackFailCount(int callbackFailCount)
	{
		this.callbackFailCount = callbackFailCount;
	}

	public String getPerformerPractitionerIdentifier()
	{
		return performerPractitionerIdentifier;
	}

	public void setPerformerPractitionerIdentifier(String performerPractitionerIdentifier)
	{
		this.performerPractitionerIdentifier = performerPractitionerIdentifier;
	}

}
