package com.takipi.oss.storage.resources.fs.base;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.folder.FolderFilesystem;

public abstract class FolderFileSystemStorageResource<T> {
    protected final FolderFilesystem<T> fs;
    
    public FolderFileSystemStorageResource(TakipiStorageConfiguration configuration) {
        this.fs = getNewFileSystem(configuration);
    }
    
    protected abstract FolderFilesystem<T> getNewFileSystem(TakipiStorageConfiguration configuration);
}
