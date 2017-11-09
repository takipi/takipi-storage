package com.takipi.oss.storage.resources.fs;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.data.simple.SimpleSearchRequest;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.api.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/storage/v1/json/simplesearch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonSimpleSearchStorageResource {

    private static final Logger logger = LoggerFactory.getLogger(JsonSimpleSearchStorageResource.class);

    private final Filesystem<?> filesystem;

    public JsonSimpleSearchStorageResource(Filesystem<?> filesystem) {
        this.filesystem = filesystem;
    }

    @POST
    @Timed
    public Response post(SimpleSearchRequest request) {
        try {
            return handleResponse(request);
        } catch (Exception e) {
            return Response.serverError().entity("Problem simple searching").build();
        }
    }

    private Response handleResponse(SimpleSearchRequest request) {
        try {
            SearchResult searchResult = filesystem.search(request);
            if(searchResult != null) {
                return Response.ok(new SimpleSearchResponse(searchResult.getData(), searchResult.getPath())).build();
            } else {
                return searchFailed(request.name);
            }
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
