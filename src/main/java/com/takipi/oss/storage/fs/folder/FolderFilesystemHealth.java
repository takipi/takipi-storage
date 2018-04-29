package com.takipi.oss.storage.fs.folder;

import java.io.File;

import com.takipi.oss.storage.fs.api.FilesystemHealth;

public class FolderFilesystemHealth implements FilesystemHealth {
	protected final File root;
	private final double maxUsedStoragePercentage;

	public FolderFilesystemHealth(String rootFolder, double maxUsedStoragePercentage) {
		this.root = new File(rootFolder);
		this.maxUsedStoragePercentage = maxUsedStoragePercentage;

		if (!folderCheck()) {
			throw new IllegalStateException("Problem with path " + rootFolder + " can't read or write");
		}
		
		if (!maxUsedStorageCheck()) {
		 	throw new IllegalStateException("Problem with path " + rootFolder + " max limit reached");
		}
	}

	public double getMaxUsedStoragePercentage() {
		return maxUsedStoragePercentage;
	}
	
	@Override
	public boolean healthy() {
		return (folderCheck() && maxUsedStorageCheck());
	}

	private boolean folderCheck() {
		return ((this.root.canRead()) && (this.root.canWrite()));
	}

	private boolean maxUsedStorageCheck() {
		if (maxUsedStoragePercentage == 0) {
			return true;
		}
		
		if ((maxUsedStoragePercentage > 1) ||
			(maxUsedStoragePercentage < 0)) {
			return false;
		}
		
		long totalSpace = this.root.getTotalSpace();

		if (totalSpace == 0) {
			return true;
		}

		long usedSpace = totalSpace - this.root.getUsableSpace();
		double usedSpacePercentage = (double)usedSpace / totalSpace;
		
		return (usedSpacePercentage <= maxUsedStoragePercentage);
	}
}
