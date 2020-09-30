package com.takipi.oss.storage.resources.fs;

import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.takipi.oss.storage.data.delete.MultiDeleteRequest;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.api.Filesystem;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/json/multidelete")
@Singleton
@Consumes({"application/json"})
@Produces({"application/json"})
public class JsonMultiDeleteStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(JsonMultiDeleteStorageResource.class);
  
  @Context
  private Filesystem<BaseRecord> filesystem;
  
  @POST
  public Response post(MultiDeleteRequest request) {
    try {
      handleResponse(request);
      return Response.ok(request.records).build();
    } catch (MultiObjectDeleteException mode) {
      return Response.serverError().entity(mode.getErrors()).build();
    } catch (Exception e) {
      return Response.serverError().entity("Error deleting keys").build();
    } 
  }
  
  private void handleResponse(MultiDeleteRequest request) throws Exception {
    try {
      this.filesystem.deleteMulti(request.records);
    } catch (Exception e) {
      logger.error("Problem deleting some of the records: " + request.records, e);
      throw e;
    } 
  }
}

