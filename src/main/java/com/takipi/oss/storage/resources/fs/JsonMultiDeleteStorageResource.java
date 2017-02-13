package com.takipi.oss.storage.resources.fs;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.takipi.oss.storage.data.delete.MultiDeleteRequest;
import com.takipi.oss.storage.data.delete.MultiDeleteResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/storage/v1/json/multidelete")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMultiDeleteStorageResource {

    private static final Logger logger = LoggerFactory.getLogger(JsonMultiDeleteStorageResource.class);

    private final Filesystem filesystem;

    public JsonMultiDeleteStorageResource(Filesystem filesystem) {
        this.filesystem = filesystem;
    }

    @POST
    @Timed
    public Response post(MultiDeleteRequest request) {
        try {
            MultiDeleteResponse response = handleResponse(request);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError().entity("Problem deleting keys").build();
        }
    }

    private MultiDeleteResponse handleResponse(MultiDeleteRequest request) {
        List<Record> deletedRecords = Lists.newArrayList();
        
        for (Record record : request.records) {
            try {
                filesystem.delete(record);
                deletedRecords.add(record);
            } catch (Exception e) {
                logger.error("Problem deleting record " + record, e);
            }
        }
        
        MultiDeleteResponse response = new MultiDeleteResponse(deletedRecords);

        return response;
    }
}
