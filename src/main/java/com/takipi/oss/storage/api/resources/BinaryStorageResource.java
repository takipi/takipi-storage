package com.takipi.oss.storage.api.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.api.fs.Filesystem;

@Path("/storage/v1/binary/{key}")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class BinaryStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(BinaryStorageResource.class);

    private Filesystem fs;

    public BinaryStorageResource(Filesystem fs) {
	this.fs = fs;
    }

    @GET
    @Timed
    public Response get(@PathParam("key") @DefaultValue("") String key) {
	if (key.equals("")) {
	    return null;
	}

	try {
	    final byte[] bytes = fs.getBytes(key);

	    StreamingOutput stream = new ByteArrayStreamingOutput(bytes);
	    
	    return Response.ok(stream).build();
	} catch (IOException e) {
	    logger.error("Problem getting key: " + key, e);
	}

	return Response.serverError().build();
    }

    @POST
    @Timed
    public Response post(@PathParam("key") @DefaultValue("") String key,
	    InputStream is) {
	if (key.equals("")) {
	    return Response.noContent().build();
	}

	try {
	    fs.putBytes(key, IOUtils.toByteArray(is));
	    return Response.ok().build();
	} catch (IOException e) {
	    logger.error("Problem putting key: " + key, e);
	}

	return Response.serverError().build();
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
