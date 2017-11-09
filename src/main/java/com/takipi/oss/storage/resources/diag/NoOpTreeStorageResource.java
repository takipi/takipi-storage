package com.takipi.oss.storage.resources.diag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

@Path("/storage/v1/diag/tree")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class NoOpTreeStorageResource {

	@GET
	@Timed
	public Response get() {
		return Response.ok("").build();
	}

}
