package com.takipi.oss.storage.resources.fs;

import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/storage/v1/binary/{serviceId}/{type}/{key:.+}")
@Singleton
@Consumes({"application/octet-stream"})
@Produces({"application/octet-stream"})
public class BinaryStorageResource {
  private static final Logger logger = LoggerFactory.getLogger(BinaryStorageResource.class);
  
  @Context
  private Filesystem<BaseRecord> filesystem;
  
  @GET
  public Response get(@PathParam("serviceId") @DefaultValue("") String serviceId, @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key) {
    if (serviceId.equals("") || type.equals("") || key.equals(""))
      return Response.status(Response.Status.BAD_REQUEST).build(); 
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
  public Response head(@PathParam("serviceId") @DefaultValue("") String serviceId, @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key) {
    if (serviceId.equals("") || type.equals("") || key.equals(""))
      return Response.status(Response.Status.BAD_REQUEST).build(); 
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
  public Response put(@PathParam("serviceId") @DefaultValue("") String serviceId, @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key, InputStream is) {
    if (serviceId.equals("") || type.equals("") || key.equals(""))
      return Response.status(Response.Status.BAD_REQUEST).build(); 
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
  public Response delete(@PathParam("serviceId") @DefaultValue("") String serviceId, @PathParam("type") @DefaultValue("") String type, @PathParam("key") @DefaultValue("") String key) {
    if (serviceId.equals("") || type.equals("") || key.equals(""))
      return Response.status(Response.Status.BAD_REQUEST).build(); 
    try {
      this.filesystem.delete((BaseRecord)Record.newRecord(serviceId, type, key));
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
    InputStream is = this.filesystem.get((BaseRecord)record);
    long size = this.filesystem.size((BaseRecord)record);
    return Response.ok(is).header("Content-Length", Long.valueOf(size)).build();
  }
  
  protected Response internalHead(Record record) throws IOException {
    long size = this.filesystem.size((BaseRecord)record);
    return Response.ok().header("Content-Length", Long.valueOf(size)).build();
  }
  
  protected Response internalPut(Record record, InputStream is) throws IOException {
    this.filesystem.put((BaseRecord)record, is);
    return Response.ok().build();
  }
  
  protected Response keyNotFound(String key) {
    return Response.status(404).entity("Key not found" + key).build();
  }
}

