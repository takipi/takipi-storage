package com.takipi.oss.storage.main;

import com.takipi.oss.storage.api.fs.Filesystem;
import com.takipi.oss.storage.api.resources.BinaryStorageResource;
import com.takipi.oss.storage.api.resources.JsonStorageResource;
import com.takipi.oss.storage.impl.fs.FolderFilesystem;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
	Filesystem fs = new FolderFilesystem(configuration.getFolderPath());

	environment.jersey().register(new BinaryStorageResource(fs));
	environment.jersey().register(new JsonStorageResource(fs));
    }
}
