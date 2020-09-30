package com.takipi.oss.storage.resource.diag;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/diag/ping")
public class PingStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(PingStorageResource.class);
  
  @GET
  @Produces({"text/plain", "application/json"})
  @Consumes({"text/plain"})
  public Response pingGet() {
    Map<String, String> resMap = new HashMap<>();
    resMap.put("status", "ok");
    try {
      return Response.status(200).entity(resMap).build();
    } catch (Exception e) {
      logger.error("Could not reply to ping request", e);
      return Response.serverError().entity("Could not reply to ping request").build();
    } 
  }
}

