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
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.helper.FilesystemUtil;
import com.takipi.oss.storage.resources.fs.base.HashFileSystemStorageResource;

@Path("/storage/v1/json/multifetch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMultiFetchStorageResource extends HashFileSystemStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(JsonMultiFetchStorageResource.class);

    public JsonMultiFetchStorageResource(TakipiStorageConfiguration configuration) {
        super(configuration);
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
                String value = FilesystemUtil.read(fs, record, request.encodingType);
                
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
