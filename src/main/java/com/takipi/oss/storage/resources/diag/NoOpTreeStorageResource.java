package com.takipi.oss.storage.resources.diag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/storage/v1/diag/tree")
@Consumes({"text/plain"})
@Produces({"application/json"})
public class NoOpTreeStorageResource {
  @GET
  public Response get() {
    return Response.ok("").build();
  }
}

