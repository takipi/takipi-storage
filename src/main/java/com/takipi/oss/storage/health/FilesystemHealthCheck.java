package com.takipi.oss.storage.health;

import com.codahale.metrics.health.HealthCheck;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.api.FilesystemHealth;
import com.takipi.oss.storage.fs.folder.FolderFilesystemHealth;

public class FilesystemHealthCheck extends HealthCheck {
    private final FilesystemHealth fsh;

    public FilesystemHealthCheck(TakipiStorageConfiguration configuration) {
        this.fsh = new FolderFilesystemHealth(configuration.getFolderPath(),
                configuration.getMaxUsedStoragePercentage());
    }

    @Override
    protected Result check() throws Exception {
        if (fsh.healthy()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Problem with filesystem");
        }
    }
}
