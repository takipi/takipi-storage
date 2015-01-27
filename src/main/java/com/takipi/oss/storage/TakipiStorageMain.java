package com.takipi.oss.storage;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.folder.HashSubfolderFilesystem;
import com.takipi.oss.storage.health.FilesystemHealthCheck;
import com.takipi.oss.storage.resources.BinaryStorageResource;

public class TakipiStorageMain extends Application<TakipiStorageConfiguration> {
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

        Filesystem fs = new HashSubfolderFilesystem(configuration.getFolderPath(),
                configuration.getMaxUsedStoragePercentage());

        environment.jersey().register(new BinaryStorageResource(fs));

        environment.healthChecks().register("filesystem", new FilesystemHealthCheck(fs));
    }

    private void enableCors(TakipiStorageConfiguration configuration, Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        cors.setInitParameter("allowedOrigins", configuration.getCorsOrigins());
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
