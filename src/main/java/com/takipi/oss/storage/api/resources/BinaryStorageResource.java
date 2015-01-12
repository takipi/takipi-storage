package com.takipi.oss.storage.api.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.oss.storage.api.fs.Filesystem;

@Path("/storage/v1/binary/{key}")
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
    protected Response internalGet(String key) throws IOException {
        final byte[] bytes = fs.getBytes(key);

        StreamingOutput stream = new ByteArrayStreamingOutput(bytes);

        return Response.ok(stream).build();
    }

    @Override
    protected Response internalPost(String key, InputStream is) throws IOException {
        fs.putBytes(key, IOUtils.toByteArray(is));
        return Response.ok().build();
    }

    protected class ByteArrayStreamingOutput implements StreamingOutput {
        private final byte[] bytes;

        protected ByteArrayStreamingOutput(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException {
            os.write(bytes);
            os.flush();
        }
    }
}
