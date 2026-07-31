package de.yoshteq.dcs.api.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestDataGenerationRequest(
	@NotNull Integer kostentraegerIk,
	@NotBlank String kvnr,
	@NotNull Long performerOrganizationIK,
	@NotBlank String performerOrganizationDisplay,
	@NotBlank String performerPractitionerIdentifier,
	@NotBlank String performerPractitionerDisplay)
{

}
