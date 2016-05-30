package com.takipi.oss.storage.fs.folder;

import java.io.File;

import com.takipi.oss.storage.fs.api.FilesystemHealth;

public class FolderFilesystemHealth implements FilesystemHealth {
    protected final File root;
    private final double maxUsedStoragePercentage;

    public FolderFilesystemHealth(String rootFolder, double maxUsedStoragePercentage) {
        this.root = new File(rootFolder);
        this.maxUsedStoragePercentage = maxUsedStoragePercentage;

        if (!healthy()) {
            throw new IllegalStateException("Problem with path " + rootFolder);
        }
    }

    @Override
    public boolean healthy() {
        return (folderCheck() && maxUsedStorageCheck());
    }

    private boolean folderCheck() {
        return ((this.root.canRead()) && (this.root.canWrite()));
    }

    private boolean maxUsedStorageCheck() {
        return ((maxUsedStoragePercentage >= 0) && (maxUsedStoragePercentage < 1) && ((this.root.getUsableSpace() / this.root
                .getTotalSpace()) <= maxUsedStoragePercentage));
    }
}
