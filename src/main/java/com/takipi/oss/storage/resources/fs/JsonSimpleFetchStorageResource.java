package com.takipi.oss.storage.resources.fs;

import com.takipi.oss.storage.data.simple.SimpleFetchRequest;
import com.takipi.oss.storage.data.simple.SimpleFetchResponse;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.helper.FilesystemUtil;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/json/simplefetch")
@Singleton
@Consumes({"application/json"})
@Produces({"application/json"})
public class JsonSimpleFetchStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(JsonSimpleFetchStorageResource.class);
  
  @Context
  private Filesystem<BaseRecord> filesystem;
  
  @POST
  public Response post(SimpleFetchRequest request) {
    try {
      return handleResponse(request);
    } catch (Exception e) {
      return Response.serverError().entity("Problem simple fetching").build();
    } 
  }
  
  private Response handleResponse(SimpleFetchRequest request) {
    try {
      String data = FilesystemUtil.read(this.filesystem, this.filesystem.pathToRecord(request.path), request.encodingType);
      if (data != null)
        return Response.ok(new SimpleFetchResponse(data)).build(); 
      logger.warn("File not found: {}", request.path);
      return Response.status(404).entity("File not found" + request.path).build();
    } catch (Exception e) {
      logger.error("Problem getting: " + request.path, e);
      return Response.serverError().entity("Problem getting " + request.path).build();
    } 
  }
}

