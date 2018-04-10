package com.takipi.oss.storage.resources.admin;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.jobs.PeriodicCleanupJob;

@Path("/storage/v1/admin/clean")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class CleanupResource {
	private static final Logger logger = LoggerFactory.getLogger(CleanupResource.class);
	
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
		return Response.ok("cleaned").build();
	}
}
