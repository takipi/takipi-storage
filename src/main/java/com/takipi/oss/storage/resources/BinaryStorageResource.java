package com.takipi.oss.storage.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;

@Path("/storage/v1/binary/{serviceId}/{type}/{key}")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class BinaryStorageResource extends StorageResource {
    private static final Logger logger = LoggerFactory.getLogger(BinaryStorageResource.class);

    public BinaryStorageResource(Filesystem fs) {
        super(fs);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Response internalGet(Record record) throws IOException {
        InputStream is = fs.getRecord(record);

        return Response.ok(is).build();
    }

    @Override
    protected Response internalPut(Record record, InputStream is) throws IOException {
        fs.putRecord(record, is);

        return Response.ok().build();
    }
}
