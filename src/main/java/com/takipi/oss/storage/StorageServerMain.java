package com.takipi.oss.storage;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.folder.HashSubfolderFilesystem;
import com.takipi.oss.storage.health.FilesystemHealthCheck;
import com.takipi.oss.storage.resources.BinaryStorageResource;
import com.takipi.oss.storage.resources.JsonStorageResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;

public class StorageServerMain extends Application<StorageServerConfiguration> {
    public static void main(String[] args) throws Exception {
        new StorageServerMain().run(args);
    }

    @Override
    public String getName() {
        return "storage-server";
    }

    @Override
    public void initialize(Bootstrap<StorageServerConfiguration> bootstrap) {

    }

    @Override
    public void run(StorageServerConfiguration configuration, Environment environment) {
        if (configuration.isEnableCors()) {
            enableCors(configuration, environment);
        }

        Filesystem fs = new HashSubfolderFilesystem(configuration.getFolderPath(),
                configuration.getMaxUsedStoragePercentage());

        environment.jersey().register(new BinaryStorageResource(fs));
        environment.jersey().register(new JsonStorageResource(fs));

        environment.healthChecks().register("filesystem", new FilesystemHealthCheck(fs));
    }

    private void enableCors(StorageServerConfiguration configuration, Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        cors.setInitParameter("allowedOrigins", configuration.getCorsOrigins());
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
