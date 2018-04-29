package com.takipi.oss.storage.resources.admin;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.jobs.PeriodicCleanupJob;

@Path("/storage/v1/admin/cleanup")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class CleanupResource {
	private final TakipiStorageConfiguration configuration;
	
	public CleanupResource(TakipiStorageConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@GET
	@Timed
	public Response get() {
		PeriodicCleanupJob periodicCleanupJob = new PeriodicCleanupJob();
		periodicCleanupJob.configure(configuration);
		periodicCleanupJob.run();
		return Response.ok("ok").build();
	}
}
