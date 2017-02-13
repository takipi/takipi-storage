package com.takipi.oss.storage.resources.fs;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/storage/v1/json/multifetch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMultiFetchStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(JsonMultiFetchStorageResource.class);

    private final Filesystem filesystem;

    public JsonMultiFetchStorageResource(Filesystem filesystem) {
        this.filesystem = filesystem;
    }

    @POST
    @Timed
    public Response post(MultiFetchRequest request) {
        try {
            MultiFetchResponse response = handleResponse(request);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }

    private MultiFetchResponse handleResponse(MultiFetchRequest request) {
        List<RecordWithData> records = Lists.newArrayList();

        for (Record record : request.records) {
            try {
                String value = FilesystemUtil.read(filesystem, record, request.encodingType);
                
                if (value != null) {
                    records.add(RecordWithData.of(record, value));
                } else {
                    logger.warn("Key not found: {}", record.getKey());
                }
            } catch (Exception e) {
                logger.error("Problem with record " + record, e);
            }
        }

        MultiFetchResponse response = new MultiFetchResponse(records);

        return response;
    }
}
