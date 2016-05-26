package com.takipi.oss.storage.resources.fs;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.simple.SimpleFetchRequest;
import com.takipi.oss.storage.data.simple.SimpleFetchResponse;
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
            SimpleFetchResponse response = handleResponse(request);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }

    private SimpleFetchResponse handleResponse(SimpleFetchRequest request) {
        String data;

        try {
            InputStream is = fs.get(request.path);
            
            data = encode(request.encodingType, is);
            
            is.close();
        } catch (Exception e) {
            logger.error("Problem with record " + request.path, e);
            data = "";
        }

        SimpleFetchResponse response = new SimpleFetchResponse(data);

        return response;
    }

    private String encode(EncodingType type, InputStream is) throws Exception {
        switch (type) {
            case PLAIN:
            case JSON: {
                return IOUtils.toString(is);
            }
            case BINARY: {
                throw new UnsupportedOperationException("not yet implemented");
                // byte[] bytes = IOUtils.toByteArray(is);
                // Base64Coder.encode(bytes);
            }
        }

        throw new Exception("problem encoding");
    }
}
