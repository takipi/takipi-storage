package com.takipi.oss.storage.resources.fs;

import com.takipi.oss.storage.data.simple.SimpleSearchRequest;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.api.SearchRequest;
import com.takipi.oss.storage.fs.api.SearchResult;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/json/simplesearch")
@Singleton
@Consumes({"application/json"})
@Produces({"application/json"})
public class JsonSimpleSearchStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(JsonSimpleSearchStorageResource.class);
  
  @Context
  private Filesystem<BaseRecord> filesystem;
  
  @POST
  public Response post(SimpleSearchRequest request) {
    try {
      return handleResponse(request);
    } catch (Exception e) {
      return Response.serverError().entity("Problem simple searching").build();
    } 
  }
  
  private Response handleResponse(SimpleSearchRequest request) {
    try {
      SearchResult searchResult = this.filesystem.search((SearchRequest)request);
      if (searchResult != null)
        return Response.ok(new SimpleSearchResponse(searchResult.getData(), searchResult.getPath())).build(); 
      return searchFailed(request.name);
    } catch (Exception e) {
      logger.error("Problem getting: " + request.name, e);
      return Response.serverError().entity("Problem getting " + request.name).build();
    } 
  }
  
  private Response searchFailed(String name) {
    logger.warn("File not found: {}", name);
    return Response.status(404).entity("File not found" + name).build();
  }
}

