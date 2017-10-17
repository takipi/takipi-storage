package com.takipi.oss.storage;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.folder.simple.SimpleFilesystem;
import com.takipi.oss.storage.fs.s3.S3Filesystem;
import com.takipi.oss.storage.health.FilesystemHealthCheck;
import com.takipi.oss.storage.resources.diag.MachineInfoOnlyStatusStorageResource;
import com.takipi.oss.storage.resources.diag.NoOpTreeStorageResource;
import com.takipi.oss.storage.resources.diag.PingStorageResource;
import com.takipi.oss.storage.resources.diag.StatusStorageResource;
import com.takipi.oss.storage.resources.diag.TreeStorageResource;
import com.takipi.oss.storage.resources.diag.VersionStorageResource;
import com.takipi.oss.storage.resources.fs.BinaryStorageResource;
import com.takipi.oss.storage.resources.fs.JsonMultiDeleteStorageResource;
import com.takipi.oss.storage.resources.fs.JsonMultiFetchStorageResource;
import com.takipi.oss.storage.resources.fs.JsonSimpleFetchStorageResource;
import com.takipi.oss.storage.resources.fs.JsonSimpleSearchStorageResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TakipiStorageMain extends Application<TakipiStorageConfiguration> {

    private final static Logger log = LoggerFactory.getLogger(TakipiStorageMain.class);

    public static void main(String[] args) throws Exception {
        new TakipiStorageMain().run(args);
    }

    @Override
    public String getName() {
        return "takipi-storage";
    }

    @Override
    public void initialize(Bootstrap<TakipiStorageConfiguration> bootstrap) {

    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void run(TakipiStorageConfiguration configuration, Environment environment) {
        if (configuration.isEnableCors()) {
            enableCors(configuration, environment);
        }

        Filesystem filesystem = configureFilesystem(configuration, environment);
        TakipiStorageConfiguration.Multifetch multifetchConfig = configuration.getMultifetch();
    
        environment.healthChecks().register("filesystem", new FilesystemHealthCheck(filesystem));
        environment.jersey().register(new BinaryStorageResource(filesystem));
        environment.jersey().register(new JsonMultiFetchStorageResource(filesystem, multifetchConfig.getConcurrencyLevel()));
        environment.jersey().register(new JsonMultiDeleteStorageResource(filesystem));
        environment.jersey().register(new JsonSimpleFetchStorageResource(filesystem));
        environment.jersey().register(new JsonSimpleSearchStorageResource(filesystem));
        environment.jersey().register(new PingStorageResource());
        environment.jersey().register(new VersionStorageResource());
    }

    private Filesystem<?> configureFilesystem(TakipiStorageConfiguration configuration, Environment environment) {
        if(configuration.hasFolderFs()) {
            return configureFolderFilesystem(configuration, environment);
        } else if(configuration.hasS3Fs()) {
            return configureS3Filesystem(configuration, environment);
        }
        else {
            throw new IllegalArgumentException("Configuration problem. Please configure either folderFs or s3Fs config property.");
        }
    }

    private SimpleFilesystem configureFolderFilesystem(TakipiStorageConfiguration configuration, Environment environment) {
        log.debug("Using local filesystem at: {}", configuration.getFolderFs().getFolderPath());

        environment.jersey().register(new TreeStorageResource(configuration));
        environment.jersey().register(new StatusStorageResource(configuration));
        return new SimpleFilesystem(configuration.getFolderFs().getFolderPath(), configuration.getFolderFs().getMaxUsedStoragePercentage());
    }

    private <T extends BaseRecord> Filesystem<T> configureS3Filesystem(TakipiStorageConfiguration configuration, Environment environment) {
        // Setup basically mocked versions of info resources.
        environment.jersey().register(new NoOpTreeStorageResource());
        environment.jersey().register(new MachineInfoOnlyStatusStorageResource());

        // Setup Amazon S3 client
        TakipiStorageConfiguration.S3Fs.Credentials credentials = configuration.getS3Fs().getCredentials();

        AmazonS3 amazonS3;
        
        if ((credentials.getAccessKey() != null) &&
            (!credentials.getAccessKey().isEmpty()) &&
            (credentials.getSecretKey() != null && !credentials.getSecretKey().isEmpty())) {
                log.debug("Using S3 Filesystem with keys" );

                AWSCredentials awsCredentials = new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey());
                amazonS3 = new AmazonS3Client(awsCredentials);
        }
        else {
            // create a client connection based on IAM role assigned
            log.debug("Using S3 Filesystem with IAM Role");
            amazonS3 = new AmazonS3Client();
        }

        // S3 bucket
        TakipiStorageConfiguration.S3Fs s3Fs = configuration.getS3Fs();
        String bucket = s3Fs.getBucket();
        String pathPrefix = s3Fs.getPathPrefix();
        log.debug("Using AWS S3 based filesystem with bucket: {}, prefix: {}", bucket, pathPrefix);
   
        return new S3Filesystem<T>(amazonS3, bucket, pathPrefix);
    }

    private void enableCors(TakipiStorageConfiguration configuration, Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        cors.setInitParameter("allowedOrigins", configuration.getCorsOrigins());
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
