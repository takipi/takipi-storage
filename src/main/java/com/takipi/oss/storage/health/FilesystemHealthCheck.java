package com.takipi.oss.storage.health;

import com.codahale.metrics.health.HealthCheck;
import com.takipi.oss.storage.fs.api.Filesystem;

public class FilesystemHealthCheck extends HealthCheck {
    private final Filesystem fs;

    public FilesystemHealthCheck(Filesystem fs) {
        this.fs = fs;
    }

    @Override
    protected Result check() throws Exception {
        if (fs.healthy()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Problem with filesystem");
        }
    }
}