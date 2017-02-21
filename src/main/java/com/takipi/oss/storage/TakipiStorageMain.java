package com.takipi.oss.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.folder.simple.SimpleFilesystem;
import com.takipi.oss.storage.fs.s3.S3Filesystem;
import com.takipi.oss.storage.resources.diag.*;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.takipi.oss.storage.health.FilesystemHealthCheck;

import com.takipi.oss.storage.resources.diag.PingStorageResource;
import com.takipi.oss.storage.resources.diag.StatusStorageResource;
import com.takipi.oss.storage.resources.diag.TreeStorageResource;
import com.takipi.oss.storage.resources.diag.VersionStorageResource;

import com.takipi.oss.storage.resources.fs.BinaryStorageResource;
import com.takipi.oss.storage.resources.fs.JsonMultiDeleteStorageResource;
import com.takipi.oss.storage.resources.fs.JsonMultiFetchStorageResource;
import com.takipi.oss.storage.resources.fs.JsonSimpleFetchStorageResource;
import com.takipi.oss.storage.resources.fs.JsonSimpleSearchStorageResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void run(TakipiStorageConfiguration configuration, Environment environment) {
        if (configuration.isEnableCors()) {
            enableCors(configuration, environment);
        }

        Filesystem filesystem = configureFilesystem(configuration, environment);

        environment.healthChecks().register("filesystem", new FilesystemHealthCheck(filesystem));
        environment.jersey().register(new BinaryStorageResource(filesystem));
        environment.jersey().register(new JsonMultiFetchStorageResource(filesystem));
        environment.jersey().register(new JsonMultiDeleteStorageResource(filesystem));
        environment.jersey().register(new JsonSimpleFetchStorageResource(filesystem));
        environment.jersey().register(new JsonSimpleSearchStorageResource(filesystem));
        environment.jersey().register(new PingStorageResource());
        environment.jersey().register(new VersionStorageResource());
    }

    private Filesystem configureFilesystem(TakipiStorageConfiguration configuration, Environment environment) {
        if(configuration.hasFolderFs()) {
            return configureFolderFilesystem(configuration, environment);
        } else if(configuration.hasS3Fs()) {
            return configureS3Filesystem(configuration, environment);
        }
        else {
            throw new IllegalArgumentException("Configuration problem. Please configure either folderFs or s3Fs config property.");
        }
    }

    private Filesystem configureFolderFilesystem(TakipiStorageConfiguration configuration, Environment environment) {
        log.debug("Using local filesystem at: {}", configuration.getFolderFs().getFolderPath());

        environment.jersey().register(new TreeStorageResource(configuration));
        environment.jersey().register(new StatusStorageResource(configuration));
        return new SimpleFilesystem(configuration.getFolderFs().getFolderPath(), configuration.getFolderFs().getMaxUsedStoragePercentage());
    }

    private Filesystem configureS3Filesystem(TakipiStorageConfiguration configuration, Environment environment) {
        // Setup basically mocked versions of info resources.
        environment.jersey().register(new NoOpTreeStorageResource());
        environment.jersey().register(new MachineInfoOnlyStatusStorageResource());

        // Setup Amazon S3 client
        TakipiStorageConfiguration.S3Fs.Credentials credentials = configuration.getS3Fs().getCredentials();
        AWSCredentials awsCredentials = new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretKey());
        AmazonS3 amazonS3 = new AmazonS3Client(awsCredentials);

        // S3 bucket
        String bucket = configuration.getS3Fs().getBucket();
        log.debug("Using AWS S3 based filesystem with bucket: {}", bucket);

        return new S3Filesystem(amazonS3, bucket);
    }

    private void enableCors(TakipiStorageConfiguration configuration, Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        cors.setInitParameter("allowedOrigins", configuration.getCorsOrigins());
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
