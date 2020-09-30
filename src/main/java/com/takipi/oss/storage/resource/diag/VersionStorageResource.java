package com.takipi.oss.storage.resource.diag;

import com.takipi.oss.storage.helper.StatusUtil;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/diag/version")
@Consumes({"*/*"})
@Produces({"application/json"})
public class VersionStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(VersionStorageResource.class);
  
  @GET
  public Response get() {
    try {
      Map<String, String> resMap = new HashMap<>();
      resMap.put("machine_version", StatusUtil.getMachineVersion());
      return Response.ok(resMap).build();
    } catch (Exception e) {
      logger.error("Could not reply to version request", e);
      return Response.serverError().entity("Could not reply to version request").build();
    } 
  }
}

