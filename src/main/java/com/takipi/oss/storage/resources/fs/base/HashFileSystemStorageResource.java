package com.takipi.oss.storage.resources.fs.base;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.folder.FolderFilesystem;
import com.takipi.oss.storage.fs.folder.record.HashSubfolderFilesystem;

public class HashFileSystemStorageResource extends FolderFileSystemStorageResource<Record> {
    public HashFileSystemStorageResource(TakipiStorageConfiguration configuration) {
        super(configuration);
    }
    
    @Override
    protected FolderFilesystem<Record> getNewFileSystem(TakipiStorageConfiguration configuration) {
        return new HashSubfolderFilesystem(configuration.getFolderPath(), configuration.getMaxUsedStoragePercentage());
    }
}
