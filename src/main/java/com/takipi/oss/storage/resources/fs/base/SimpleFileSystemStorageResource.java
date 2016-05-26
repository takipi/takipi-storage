package com.takipi.oss.storage.resources.fs.base;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.folder.simple.SimpleFilesystem;

public class SimpleFileSystemStorageResource extends FileSystemStorageResource<String> {
    public SimpleFileSystemStorageResource(TakipiStorageConfiguration configuration) {
        super(configuration);
    }
    
    @Override
    protected Filesystem<String> getNewFileSystem(TakipiStorageConfiguration configuration) {
        return new SimpleFilesystem(configuration.getFolderPath(), configuration.getMaxUsedStoragePercentage());
    }
}
