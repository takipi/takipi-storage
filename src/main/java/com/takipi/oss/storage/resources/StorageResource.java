package com.takipi.oss.storage.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;

public abstract class StorageResource {
    protected Filesystem fs;

    public StorageResource(Filesystem fs) {
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
        } catch (IOException e) {
            getLogger().error("Problem getting key: " + key, e);
        }

        return Response.serverError().entity("Problem getting key " + key).build();
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
        } catch (IOException e) {
            getLogger().error("Problem putting key: " + key, e);
        }

        return Response.serverError().entity("Problem putting key " + key).build();
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
        } catch (IOException e) {
            getLogger().error("Problem deleting key: " + key, e);
        }

        return Response.serverError().entity("Problem deleting key " + key).build();
    }

    protected abstract Response internalGet(Record record) throws IOException;

    protected abstract Response internalPut(Record record, InputStream is) throws IOException;

    protected abstract Logger getLogger();

}
