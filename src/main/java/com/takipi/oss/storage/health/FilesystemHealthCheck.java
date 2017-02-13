package com.takipi.oss.storage.health;

import com.codahale.metrics.health.HealthCheck;
import com.takipi.oss.storage.fs.api.Filesystem;

public class FilesystemHealthCheck extends HealthCheck {
    private final Filesystem filesystem;

    public FilesystemHealthCheck(Filesystem filesystem) {
        this.filesystem = filesystem;
    }

    @Override
    protected Result check() throws Exception {
        if (filesystem.healthy()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Problem with filesystem");
        }
    }
}
