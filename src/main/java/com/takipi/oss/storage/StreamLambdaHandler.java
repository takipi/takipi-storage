package com.takipi.oss.storage;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.s3.S3Filesystem;
import com.takipi.oss.storage.helper.CORSFilter;
import com.takipi.oss.storage.resource.diag.MachineInfoOnlyStatusStorageResource;
import com.takipi.oss.storage.resource.diag.NoOpTreeStorageResource;
import com.takipi.oss.storage.resource.diag.PingStorageResource;
import com.takipi.oss.storage.resource.diag.VersionStorageResource;
import com.takipi.oss.storage.resources.fs.BinaryStorageResource;
import com.takipi.oss.storage.resources.fs.JsonMultiDeleteStorageResource;
import com.takipi.oss.storage.resources.fs.JsonMultiFetchStorageResource;
import com.takipi.oss.storage.resources.fs.JsonSimpleFetchStorageResource;
import com.takipi.oss.storage.resources.fs.JsonSimpleSearchStorageResource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamLambdaHandler implements RequestStreamHandler {
  private static final Logger LOG = LoggerFactory.getLogger(StreamLambdaHandler.class);
  
  private static final TakipiStorageConfiguration CONFIGURATION = (new TakipiStorageConfiguration()).init();
  
  private static final S3Filesystem<Record> FILESYSTEM = configureFilesystem();
  
  private static final ResourceConfig JERSEY_APPLICATION = initResource();
  
  private static final JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> HANDLER = JerseyLambdaContainerHandler.getAwsProxyHandler((Application)JERSEY_APPLICATION);
  
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
    HANDLER.proxyStream(inputStream, outputStream, context);
    outputStream.close();
  }
  
  private static S3Filesystem<Record> configureFilesystem() {
    return configureS3Filesystem();
  }
  
  private static S3Filesystem<Record> configureS3Filesystem() {
    AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
    TakipiStorageConfiguration.S3Fs s3Fs = CONFIGURATION.getS3Fs();
    String bucket = s3Fs.getBucket();
    String pathPrefix = s3Fs.getPathPrefix();
    LOG.debug("Using AWS S3 based filesystem with bucket: {}, prefix: {}", bucket, pathPrefix);
    return new S3Filesystem(amazonS3, bucket, pathPrefix);
  }
  
  private static ResourceConfig initResource() {
    ResourceConfig jappl = (new ResourceConfig()).register(JacksonFeature.class).register(BinaryStorageResource.class).register(JsonMultiFetchStorageResource.class).register(JsonMultiDeleteStorageResource.class).register(JsonSimpleFetchStorageResource.class).register(JsonSimpleSearchStorageResource.class).register(PingStorageResource.class).register(VersionStorageResource.class).register(NoOpTreeStorageResource.class).register(MachineInfoOnlyStatusStorageResource.class).register(new AbstractBinder() {
          protected void configure() {
            ((InstanceBinding)bind(StreamLambdaHandler.FILESYSTEM).to(Filesystem.class)).in(Singleton.class);
            ((InstanceBinding)bind(StreamLambdaHandler.CONFIGURATION.getMultifetch()).to(TakipiStorageConfiguration.Multifetch.class)).in(Singleton.class);
          }
        });
    if (CONFIGURATION.isEnableCors())
      jappl.register(new CORSFilter(CONFIGURATION.getCorsOrigins())); 
    return jappl;
  }
}

