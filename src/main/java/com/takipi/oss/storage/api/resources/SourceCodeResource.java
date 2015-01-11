package com.takipi.oss.storage.api.resources;

import io.dropwizard.servlets.assets.ResourceNotFoundException;

import java.io.IOException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.oss.storage.api.fs.Filesystem;

@Path("/{serviceId}/sourcecode")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class SourceCodeResource {
    private static final Logger logger = LoggerFactory.getLogger(SourceCodeResource.class);
    
    private Filesystem fs;
    
    @GET
    public byte[] fetch(@PathParam("serviceId") String serviceId,
	    @QueryParam("checksum") @DefaultValue("") String checksum) {
	logger.debug("get fetch {} {}", serviceId, checksum);
	
	if (checksum.equals("")) {
	    return null;
	}
	
	try {
	    return fs.get("", checksum);
	} catch (IOException e) {
	    throw new ResourceNotFoundException(e);
	}
    }
}
