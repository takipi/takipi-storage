package com.takipi.oss.storage.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.oss.storage.fs.api.Filesystem;

@Path("/storage/v1/json/{key}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonStorageResource  extends StorageResource {
    private static final Logger logger = LoggerFactory.getLogger(JsonStorageResource.class);

    public JsonStorageResource(Filesystem fs) {
        super(fs);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
    
    @Override
    protected Response internalGet(String key) throws IOException {
        final String json = fs.getJson(key);

        return Response.ok(json).build();
    }

    @Override
    protected Response internalPost(String key, InputStream is) throws IOException {
        fs.putJson(key, IOUtils.toString(is));

        return Response.ok().build();
    }
}
