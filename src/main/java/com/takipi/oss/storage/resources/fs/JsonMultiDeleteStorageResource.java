package com.takipi.oss.storage.resources.fs;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.data.delete.MultiDeleteRequest;
import com.takipi.oss.storage.data.delete.MultiDeleteResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.resources.fs.base.HashFileSystemStorageResource;

@Path("/storage/v1/json/multidelete")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMultiDeleteStorageResource extends HashFileSystemStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(JsonMultiDeleteStorageResource.class);

    public JsonMultiDeleteStorageResource(TakipiStorageConfiguration configuration) {
        super(configuration);
    }

    @POST
    @Timed
    public Response post(MultiDeleteRequest request) {
        try {
            MultiDeleteResponse response = handleResponse(request);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }

    private MultiDeleteResponse handleResponse(MultiDeleteRequest request) {
        List<Record> deletedRecords = Lists.newArrayList();
        
        for (Record record : request.records) {
            try {
                fs.delete(record);
                deletedRecords.add(record);
            } catch (Exception e) {
                logger.error("Problem deleting record " + record, e);
            }
        }
        
        MultiDeleteResponse response = new MultiDeleteResponse(deletedRecords);

        return response;
    }
}
