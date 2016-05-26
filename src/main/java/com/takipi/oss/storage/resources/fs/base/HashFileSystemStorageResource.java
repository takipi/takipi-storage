package com.takipi.oss.storage.resources.fs.base;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.folder.HashSubfolderFilesystem;

public class HashFileSystemStorageResource extends FileSystemStorageResource {
    public HashFileSystemStorageResource(TakipiStorageConfiguration configuration) {
        super(configuration);
    }
    
    @Override
    protected Filesystem getNewFileSystem(TakipiStorageConfiguration configuration) {
        return new HashSubfolderFilesystem(configuration.getFolderPath(), configuration.getMaxUsedStoragePercentage());
    }
}
