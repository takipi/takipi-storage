package com.takipi.oss.storage.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.health.FilesystemHealthCheck;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;

@Every("${cleanup}")
public class PeriodicCleanupJob extends Job {
	private static final Logger logger = LoggerFactory.getLogger(PeriodicCleanupJob.class);
	
	protected static final int MAX_FILES_FOR_CLEANUP_STATS = 10000;
	protected static final String[] PREFIXES_SAFE_TO_REMOVE = {
		"HYB_HIT_", 
		"HYB_CER_", 
		"HYB_OM_", 
		"HYB_WTGR_", 
		"HYB_SAFE_"
	};
	
	protected static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static volatile CleanupStats lastCleanupStats = null;
	
	private Path rootFolder;
	private FilesystemHealthCheck fileSystemHealthCheck;
	private int retentionPeriodDays;
	private boolean cleanupJobEnabled;
	
	public void configure(TakipiStorageConfiguration configuration) {
		String rootFolderPath = configuration.getFolderPath();
		
		if ((rootFolderPath == null) ||
			(rootFolderPath.isEmpty())) {
			return;
		}
		
		rootFolder = Paths.get(rootFolderPath);
		fileSystemHealthCheck = new FilesystemHealthCheck(configuration);
		retentionPeriodDays = configuration.getRetentionPeriodDays();
		cleanupJobEnabled = configuration.isCleanupJobEnabled();
		
		if (retentionPeriodDays <= 0) {
			logger.info("Disabling cleanup job, retention period days is {}", retentionPeriodDays);
		} else if (!cleanupJobEnabled) {
			logger.info("Disabling cleanup job, cleanupJobEnabled flag is false");
		} else if (rootFolder == null) {
			logger.info("Disabling cleanup job, invalid root directory", rootFolder);
		}
	}
	
	@Override
	public void doJob(JobExecutionContext context) throws JobExecutionException {
		run();
	}
	
	public void run() {
		if ((rootFolder == null) ||
			(retentionPeriodDays <= 0) ||
			(!cleanupJobEnabled)) {
			return;
		}
		
		long startMillis = System.currentTimeMillis();
		long retentionPeriodDaysInMillis = TimeUnit.MILLISECONDS.convert(retentionPeriodDays, TimeUnit.DAYS);
		long minimumTimeMillis = startMillis - retentionPeriodDaysInMillis;
		long currentRetentionPeriodDays = retentionPeriodDays;
		List<File> removedFiles = new LinkedList<>();
		
		while (true) {
			logger.info("About to clean storage files. (folder: {}) (retentionPeriodDays: {}) " +
				"(maxUsedPercentage: {}) (minimumTimeMillis: {})",
				rootFolder, currentRetentionPeriodDays, 
				fileSystemHealthCheck.getMaxUsedStoragePercentage(), minimumTimeMillis);
			
			if (currentRetentionPeriodDays < 1) {
				logger.warn("Cleanup stopped, retention period too small");
				break;
			}
			
			try {
				List<File> removedFilesFromLastIteration = cleanFiles(minimumTimeMillis, 
					MAX_FILES_FOR_CLEANUP_STATS - removedFiles.size());
				
				removedFiles.addAll(removedFilesFromLastIteration);
			} catch (Exception e) {
				logger.error("An error occured during file cleanup", e);
				break;
			}
			
			if (fileSystemHealthCheck.execute().isHealthy()) {
				break;
			}
			
			// an optimization to save resources, if the initial retentionPeriodDays was big
			//
			if (currentRetentionPeriodDays > 20) {
				currentRetentionPeriodDays = (long)(0.75 * currentRetentionPeriodDays);
			} else {
				currentRetentionPeriodDays--;
			}
			
			retentionPeriodDaysInMillis = TimeUnit.MILLISECONDS.convert(currentRetentionPeriodDays, TimeUnit.DAYS);
			minimumTimeMillis = startMillis - retentionPeriodDaysInMillis;
		}
		
		long durationMillis = System.currentTimeMillis() - startMillis;
		
		logger.info("Cleanup finished, (removed files: {})", removedFiles.size());
		PeriodicCleanupJob.lastCleanupStats = new CleanupStats(startMillis, durationMillis, removedFiles);
	}
	
	private List<File> cleanFiles(final long minimumTimeMillis, 
		final int maximumRemovedFiles) throws Exception {
		final List<File> removedFiles = new LinkedList<>();
		
		Files.walkFileTree(rootFolder, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				if (!StringUtils.startsWithAny(path.getFileName().toString(), PREFIXES_SAFE_TO_REMOVE)) {
					return FileVisitResult.CONTINUE;
				}
				
				File file = path.toFile();
				
				if (file.lastModified() > minimumTimeMillis) {
					return FileVisitResult.CONTINUE;
				}
				
				if (FileUtils.deleteQuietly(file)) {
					if (removedFiles.size() < maximumRemovedFiles) {
						removedFiles.add(file);
					}
				}
				
				return FileVisitResult.CONTINUE;
			}
		});
		
		return removedFiles;
	}
	
	public static class CleanupStats {
		private final long startEpochTime;
		private final long durationMillis;
		private final List<File> removedFiles;
		
		public CleanupStats(long startEpochTime, long durationMillis, List<File> removedFiles) {
			this.startEpochTime = startEpochTime;
			this.durationMillis = durationMillis;
			this.removedFiles = removedFiles;
		}
		
		public String getFormattedStartTime() {
			return timeFormat.format(new Date(startEpochTime));
		}
		
		public long getDurationMillis() {
			return durationMillis;
		}
		
		public List<File> getRemovedFiles() {
			return removedFiles;
		}
	}
}
