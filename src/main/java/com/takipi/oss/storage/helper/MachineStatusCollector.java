package com.takipi.oss.storage.helper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.takipi.oss.storage.data.status.MachineStatus;
import com.takipi.oss.storage.jobs.PeriodicCleanupJob;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MachineStatusCollector {
	private static final Logger logger = LoggerFactory.getLogger(MachineStatusCollector.class);
	
	private static final String hitsSizeName 			= "hits size";
	private static final String hitsCountName 			= "hits count";
	private static final String hitsDirectoryName	 	= "hits";
	
	private static final String namersSizeName 			= "namers size";
	private static final String namersCountName 		= "namers count";
	private static final String namersDirectoryName	 	= "silver-namer";
	
	private static final String sourceCodesSizeName 	= "sources size";
	private static final String sourceCodesCountName 	= "sources count";
	private static final String sourceCodeDirectoryName	= "source-code";
	
	private static final String cerebroSizeName 		= "cerebro size";
	private static final String cerebroCountName 		= "cerebro count";
	private static final String cerebroDirectoryName 	= "cerebro";
	
	private static final String overmindSizeName 		= "overmind size";
	private static final String overmindCountName 		= "overmind count";
	private static final String overmindDirectoryName 	= "overmind";
	
	private static final String tigersSizeName 			= "tigers size";
	private static final String tigersCountName 		= "tigers count";
	private static final String tigersDirectoryName 	= "white-tiger";
	
	private final String basePath;
	
	public MachineStatusCollector(String basePath) {
		this.basePath = basePath;
	}
	
	public MachineStatus collect() {
		try {
			MachineStatus machineStatus = new MachineStatus();
			
			collectMachineInfo(machineStatus);
			collectStorageInfo(machineStatus);
			collectLastCleanupInfo(machineStatus);
			
			return machineStatus;
		} catch (Exception e) {
			logger.error("Error collecting machine status.", e);
			return null;
		}
	}
	
	private void collectStorageInfo(MachineStatus machineStatus) {
		File directory = new File(basePath);
		Map<String, Long> mappedData = traverseTreeForData(directory);

		machineStatus.setHitsSizeBytes(mappedData.get(hitsSizeName));
		machineStatus.setHitsCount(mappedData.get(hitsCountName));

		machineStatus.setNamersSizeBytes(mappedData.get(namersSizeName));
		machineStatus.setNamersCount(mappedData.get(namersCountName));

		machineStatus.setSourceCodeSizeBytes(mappedData.get(sourceCodesSizeName));
		machineStatus.setSourceCodeCount(mappedData.get(sourceCodesCountName));

		machineStatus.setCerebroSizeBytes(mappedData.get(cerebroSizeName));
		machineStatus.setCerebroCount(mappedData.get(cerebroCountName));

		machineStatus.setOvermindSizeBytes(mappedData.get(overmindSizeName));
		machineStatus.setOvermindCount(mappedData.get(overmindCountName));

		machineStatus.setTigersSizeBytes(mappedData.get(tigersSizeName));
		machineStatus.setTigersCount(mappedData.get(tigersCountName));

		machineStatus.setFreeSpaceLeftBytes(directory.getFreeSpace());
	}
	
	private void collectLastCleanupInfo(MachineStatus machineStatus) {
		PeriodicCleanupJob.CleanupStats cleanupStats = PeriodicCleanupJob.lastCleanupStats;
		
		machineStatus.setLastCleanupStartTime(cleanupStats.getFormattedStartTime());
		machineStatus.setLastCleanupDurationMillis(cleanupStats.getDurationMillis());
		machineStatus.setLastCleanupRemovedFiles(cleanupStats.getRemovedFiles());
	}
	
	private Map<String, Long> traverseTreeForData(File directory) {
		Map<String, Long> map = initializeMapForData();
		
		if (directory.isDirectory()) {
			traverseTreeForData(directory, map);
		}
		
		return map;
	}
	
	// Traverse into the directory and its subdirectories,
	// avoiding any hidden or visible files until finding
	// the suitable directory (hits/namers/source-code).
	private void traverseTreeForData(File directory, Map<String, Long> map) {
		for (File file : directory.listFiles()) {
			if (file.isHidden() || !file.isDirectory()) {
				continue;
			}
			
			handleDirectory(map, file);
		}
	}
	
	// Direct hits/namers/source-code directory to its handler
	// or keep traversing.
	private void handleDirectory(Map<String, Long> map, File directory) {
		switch (directory.getName()) {
			case hitsDirectoryName: {
				handleSpecialDirectory(directory, hitsSizeName, hitsCountName, map);
				break;
			}
			case namersDirectoryName: {
				handleSpecialDirectory(directory, namersSizeName, namersCountName, map);
				break;
			}
			case sourceCodeDirectoryName: {
				handleSpecialDirectory(directory, sourceCodesSizeName, sourceCodesCountName, map);
				break;
			}
			case cerebroDirectoryName: {
				handleSpecialDirectory(directory, cerebroSizeName, cerebroCountName, map);
				break;
			}
			case overmindDirectoryName: {
				handleSpecialDirectory(directory, overmindSizeName, overmindCountName, map);
				break;
			}
			case tigersDirectoryName: {
				handleSpecialDirectory(directory, tigersSizeName, tigersCountName, map);
				break;
			}
			default: {
				traverseTreeForData(directory, map);
				break;
			}
		}
	}
	
	// Extract data of visible files.
	private void handleSpecialDirectory(File directory, String sizeName, String countName, Map<String, Long> map) {
		for (File file : directory.listFiles()) {
			if (file.isHidden()) {
				continue;
			}
			
			if (file.isDirectory()) {
				handleSpecialDirectory(file, sizeName, countName, map);
			} else {
				long sizeValue = map.get(sizeName);
				long countValue = map.get(countName);
				
				map.put(sizeName, sizeValue + safeFileSize(file));
				map.put(countName, countValue + 1);
			}
		}
	}
	
	private long safeFileSize(File file) {
		try {
			return FileUtils.sizeOf(file);
		} catch (Exception e) {
			return 0;
		}
	}
	
	private Map<String, Long> initializeMapForData() {
		Map<String, Long> result = new HashMap<>();
		
		result.put(hitsSizeName, 0l);
		result.put(hitsCountName, 0l);
		result.put(namersSizeName, 0l);
		result.put(namersCountName, 0l);
		result.put(sourceCodesSizeName, 0l);
		result.put(sourceCodesCountName, 0l);
		result.put(cerebroSizeName, 0l);
		result.put(cerebroCountName, 0l);
		result.put(overmindSizeName, 0l);
		result.put(overmindCountName, 0l);
		result.put(tigersSizeName, 0l);
		result.put(tigersCountName, 0l);
		
		return result;
	}
	
	private void collectMachineInfo(MachineStatus machineStatus) {
		machineStatus.setMachineName(StatusUtil.getMachineName());
		machineStatus.setPid(StatusUtil.getProcessId());
		machineStatus.setJvmUpTimeMillis(StatusUtil.getJvmUpTimeInMilli());
		machineStatus.setAvailableProcessors(StatusUtil.getAvailableProcessors());
		machineStatus.setLoadAverage(StatusUtil.getLoadAvg());
		machineStatus.setProcessCpuLoad(StatusUtil.getProcessCpuLoad());
		machineStatus.setTotalRamBytes(StatusUtil.getTotalRamInBytes());
		machineStatus.setUsedRamBytes(StatusUtil.getUsedRamInBytes());
		machineStatus.setHeapSizeBytes(StatusUtil.getHeapSizeInBytes());
		machineStatus.setPermGenSizeBytes(StatusUtil.getPermGenSizeInBytes());
		machineStatus.setVersion(StatusUtil.getMachineVersion());
	}
}
