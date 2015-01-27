package com.takipi.oss.storage.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;

@Path("/storage/v1/binary/{serviceId}/{type}/{key}")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class BinaryStorageResource {
    private static final Logger logger = LoggerFactory.getLogger(BinaryStorageResource.class);

    protected Filesystem fs;

    public BinaryStorageResource(Filesystem fs) {
        this.fs = fs;
    }

    @GET
    @Timed
    public Response get(@PathParam("serviceId") @DefaultValue("") String serviceId,
            @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key) {
        if (serviceId.equals("") || type.equals("") || key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            return internalGet(Record.newRecord(serviceId, type, key));
        } catch (FileNotFoundException e) {
            logger.warn("Key not found: {}", key);
            return keyNotFound(key);
        } catch (Exception e) {
            logger.error("Problem getting key: " + key, e);
            return Response.serverError().entity("Problem getting key " + key).build();
        }
    }

    @HEAD
    @Timed
    public Response head(@PathParam("serviceId") @DefaultValue("") String serviceId,
            @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key) {
        if (serviceId.equals("") || type.equals("") || key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            return internalHead(Record.newRecord(serviceId, type, key));
        } catch (FileNotFoundException e) {
            logger.warn("Key not found: {}", key);
            return keyNotFound(key);
        } catch (Exception e) {
            logger.error("Problem checking key: " + key, e);
            return Response.serverError().entity("Problem checking key " + key).build();
        }
    }

    @PUT
    @Timed
    public Response put(@PathParam("serviceId") @DefaultValue("") String serviceId,
            @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key,
            InputStream is) {
        if (serviceId.equals("") || type.equals("") || key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            return internalPut(Record.newRecord(serviceId, type, key), is);
        } catch (FileNotFoundException e) {
            logger.warn("Key not found: {}", key);
            return keyNotFound(key);
        } catch (Exception e) {
            logger.error("Problem putting key: " + key, e);
            return Response.serverError().entity("Problem putting key " + key).build();
        }
    }

    @DELETE
    @Timed
    public Response delete(@PathParam("serviceId") @DefaultValue("") String serviceId,
            @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key) {
        if (serviceId.equals("") || type.equals("") || key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            fs.delete(Record.newRecord(serviceId, type, key));
            return Response.ok().build();
        } catch (FileNotFoundException e) {
            logger.warn("Key not found: {}", key);
            return keyNotFound(key);
        } catch (Exception e) {
            logger.error("Problem deleting key: " + key, e);
            return Response.serverError().entity("Problem deleting key " + key).build();
        }
    }

    protected Response internalGet(Record record) throws IOException {
        InputStream is = fs.get(record);

        return Response.ok(is).build();
    }

    protected Response internalHead(Record record) throws IOException {
        fs.exists(record);
        
        return Response.ok().build();
    }

    protected Response internalPut(Record record, InputStream is) throws IOException {
        fs.put(record, is);

        return Response.ok().build();
    }

    protected Response keyNotFound(String key) {
        return Response.status(404).entity("Key not found" + key).build();
    }
}
