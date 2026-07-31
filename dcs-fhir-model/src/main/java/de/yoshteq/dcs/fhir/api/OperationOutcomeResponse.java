package de.yoshteq.dcs.fhir.api;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.CodeableConcept;

public class OperationOutcomeResponse
{

	public static OperationOutcome allOk()
	{
		var issue = new OperationOutcomeIssueComponent()
			.setSeverity(IssueSeverity.INFORMATION)
			.setCode(IssueType.INFORMATIONAL)
			.setDetails(new CodeableConcept().setText("All OK"));

		var outcome = new OperationOutcome();
		outcome.setId("allok");
		outcome.addIssue(issue);
		return outcome;
	}

	private OperationOutcomeResponse()
	{
	}

}
