package com.takipi.oss.storage.helper;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class CORSFilter implements ContainerResponseFilter {
  private static final Logger log = LoggerFactory.getLogger(ContainerResponseFilter.class);
  
  private static String CORSOrigin;
  
  public CORSFilter(String CORSOrigin) {
    CORSFilter.CORSOrigin = CORSOrigin;
  }
  
  public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
    response.getHeaders().add("Access-Control-Allow-Origin", CORSOrigin);
    response.getHeaders().add("Access-Control-Allow-Headers", "X-Requested-With,Content-Type,Accept,Origin,Authorization");
    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
    response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
  }
}

