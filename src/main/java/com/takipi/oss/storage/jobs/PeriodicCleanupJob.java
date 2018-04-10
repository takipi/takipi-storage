package com.takipi.oss.storage.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;

import com.takipi.oss.storage.health.FilesystemHealthCheck;
import com.takipi.oss.storage.TakipiStorageConfiguration;

@Every("${cleanup}")
public class PeriodicCleanupJob extends Job {
	private final String[] PREFIXES_SAFE_TO_REMOVE = new String[] {
		"HYB_HIT_", 
		"HYB_CER_", 
		"HYB_OM_", 
		"HYB_WTGR_", 
		"HYB_SAFE_"
	};
	
	private Path rootFolder;
	private FilesystemHealthCheck fileSystemHealthCheck;
	private int retentionPeriodDays;
	
	public void configure(TakipiStorageConfiguration configuration) {
		String rootFolderPath = configuration.getFolderPath();
		
		if (rootFolderPath == null || rootFolderPath.isEmpty()) {
			return;
		}
		
		rootFolder = Paths.get(rootFolderPath);
		fileSystemHealthCheck = new FilesystemHealthCheck(configuration);
		retentionPeriodDays = configuration.getRetentionPeriodDays();
	}
	
	@Override
	public void doJob(JobExecutionContext context) throws JobExecutionException {
		run();
	}
	
	public void run() {
		if (rootFolder == null) {
			return;
		}
		
		long retentionPeriodDaysInMillis = TimeUnit.MILLISECONDS.convert(retentionPeriodDays, TimeUnit.DAYS);
		long minimumTimeMillis = System.currentTimeMillis() - retentionPeriodDaysInMillis;
		
		System.out.println("RUN EVERY folder: " + rootFolder);
		System.out.println("RUN EVERY period: " + retentionPeriodDays);
		System.out.println("RUN EVERY retentionPeriodDaysInMillis: " + retentionPeriodDaysInMillis);
		System.out.println("RUN EVERY minimumTimeMillis: " + minimumTimeMillis);
		System.out.println("RUN EVERY healthy: " + fileSystemHealthCheck.execute().isHealthy());
		
		try {
			Files.walkFileTree(rootFolder, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					if (!StringUtils.startsWithAny(path.getFileName().toString(), PREFIXES_SAFE_TO_REMOVE)) {
						return FileVisitResult.CONTINUE;
					}
					
					File file = path.toFile();
					
					System.out.println(file + " - " + file.lastModified());
					
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
