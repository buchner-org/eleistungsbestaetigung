package de.yoshteq.dcs.api.test;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

public interface TestApi
{

	@POST
	@Path("/confirmation-requests")
	Long createConfirmationRequest(TestConfirmationRequest request);

	@GET
	@Path("/confirmation-requests")
	List<ConfirmationRequestSummary> getConfirmationRequests();

	@GET
	@Path("/confirmation-requests/{id}")
	ConfirmationRequestSummary getConfirmationRequest(@PathParam("id") Long id);

	@DELETE
	@Path("/confirmation-requests/{id}")
	void deleteConfirmationRequest(@PathParam("id") Long id);

	@POST
	@Path("/generate-data")
	List<TestConfirmationRequest> createConfirmationRequest(
		@QueryParam("serviceType") TestDataServiceType serviceType,
		TestDataGenerationRequest request);

}