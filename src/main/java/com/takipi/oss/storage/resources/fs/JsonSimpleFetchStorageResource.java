package com.takipi.oss.storage.resources.fs;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.data.simple.SimpleFetchRequest;
import com.takipi.oss.storage.data.simple.SimpleFetchResponse;
import com.takipi.oss.storage.helper.FilesystemUtil;
import com.takipi.oss.storage.resources.fs.base.SimpleFileSystemStorageResource;

@Path("/storage/v1/json/simplefetch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonSimpleFetchStorageResource extends SimpleFileSystemStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(JsonSimpleFetchStorageResource.class);

    public JsonSimpleFetchStorageResource(TakipiStorageConfiguration configuration) {
        super(configuration);
    }

    @POST
    @Timed
    public Response post(SimpleFetchRequest request) {
        try {
            return handleResponse(request);
        } catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }

    private Response handleResponse(SimpleFetchRequest request) {
        try {
            String data = FilesystemUtil.read(fs, request.path, request.encodingType);
            
            if (data != null) {
                return Response.ok(new SimpleFetchResponse(data)).build();
            } else {
                logger.warn("File not found: {}", request.path);
                return Response.status(404).entity("File not found" + request.path).build();
            }
        } catch (Exception e) {
            logger.error("Problem getting: " + request.path, e);
            return Response.serverError().entity("Problem getting " + request.path).build();
        }
    }
}
