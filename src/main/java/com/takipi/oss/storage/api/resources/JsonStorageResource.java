package com.takipi.oss.storage.api.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.api.fs.Filesystem;

@Path("/storage/v1/json/{key}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(JsonStorageResource.class);

    private Filesystem fs;

    public JsonStorageResource(Filesystem fs) {
	this.fs = fs;
    }

    @GET
    @Timed
    public Response get(@PathParam("key") @DefaultValue("") String key) {
	if (key.equals("")) {
	    return Response.status(Status.BAD_REQUEST).build();
	}

	try {
	    final String json = fs.getJson(key);

	    return Response.ok(json).build();
	} catch (IOException e) {
	    logger.error("Problem getting key: " + key, e);
	}

	return Response.serverError().entity("Problem getting key " + key).build();
    }

    @POST
    @Timed
    public Response post(@PathParam("key") @DefaultValue("") String key,
	    InputStream is) {
	if (key.equals("")) {
	    return Response.status(Status.BAD_REQUEST).build();
	}

	try {
	    fs.putJson(key, IOUtils.toString(is));
	    
	    return Response.ok().build();
	} catch (IOException e) {
	    logger.error("Problem putting key: " + key, e);
	}

	return Response.serverError().entity("Problem putting key " + key).build();
    }
}
