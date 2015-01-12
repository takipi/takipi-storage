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
import com.takipi.oss.storage.fs.api.Filesystem;

public abstract class StorageResource {
    protected Filesystem fs;

    public StorageResource(Filesystem fs) {
        this.fs = fs;
    }

    @GET
    @Timed
    public Response get(@PathParam("key") @DefaultValue("") String key) {
        if (key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            return internalGet(key);
        } catch (IOException e) {
            getLogger().error("Problem getting key: " + key, e);
        }

        return Response.serverError().entity("Problem getting key " + key).build();
    }

    @PUT
    @Timed
    public Response put(@PathParam("key") @DefaultValue("") String key, InputStream is) {
        if (key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            return internalPut(key, is);
        } catch (IOException e) {
            getLogger().error("Problem putting key: " + key, e);
        }

        return Response.serverError().entity("Problem putting key " + key).build();
    }

    @DELETE
    @Timed
    public Response delete(@PathParam("key") @DefaultValue("") String key) {
        if (key.equals("")) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        try {
            fs.delete(key);
            return Response.ok().build();
        } catch (IOException e) {
            getLogger().error("Problem deleting key: " + key, e);
        }

        return Response.serverError().entity("Problem deleting key " + key).build();
    }

    protected abstract Response internalGet(String key) throws IOException;

    protected abstract Response internalPut(String key, InputStream is) throws IOException;

    protected abstract Logger getLogger();
}
