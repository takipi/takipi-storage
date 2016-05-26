package com.takipi.oss.storage.resources.fs.base;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.api.Filesystem;

public abstract class FileSystemStorageResource {
    protected Filesystem fs;
    
    public FileSystemStorageResource(TakipiStorageConfiguration configuration) {
        this.fs = getNewFileSystem(configuration);
    }
    
    protected abstract Filesystem getNewFileSystem(TakipiStorageConfiguration configuration);
}
