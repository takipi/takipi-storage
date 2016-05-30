package com.takipi.oss.storage.fs.api;

public interface FilesystemHealth {
    /**
     * Checks if the filesystem is healthy. This will be called during health
     * check
     * 
     * @return true iff the filesystem is healthy
     */
    boolean healthy();
}
