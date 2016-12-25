package com.takipi.oss.storage.resources.diag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.helper.StatusUtil;

@Path("/storage/v1/diag/version")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class VersionStorageResource {
	private static final Logger logger = LoggerFactory.getLogger(VersionStorageResource.class);
	
	@GET
	@Timed
	public Response get() {
		try {
			String implVersion = StatusUtil.getMachineVersion();
			
			return Response.ok(implVersion).build();
		} catch (Exception e) {
			logger.error("Could not reply to version request", e);
			return Response.serverError().entity("Could not reply to version request").build();
		}
	}
}
