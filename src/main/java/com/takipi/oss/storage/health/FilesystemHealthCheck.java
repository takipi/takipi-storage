package com.takipi.oss.storage.health;

import com.codahale.metrics.health.HealthCheck;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.fs.folder.FolderFilesystemHealth;

public class FilesystemHealthCheck extends HealthCheck {
	private final FolderFilesystemHealth fsh;

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
	
	public double getMaxUsedStoragePercentage() {
		return fsh.getMaxUsedStoragePercentage();
	}
}
