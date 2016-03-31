package com.takipi.oss.storage.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.helper.StatusUtil;

@Path("/storage/v1/diag/status")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class StatusStorageResource {
	private static final Logger logger = LoggerFactory.getLogger(StatusStorageResource.class);
	private static final String hitsSizeName 			= "HitsSize";
	private static final String hitsCountName 			= "HitsCount";
	private static final String hitsDirectoryName	 	= "hits";
	private static final String namersSizeName 			= "namersSize";
	private static final String namersCountName 		= "namersCount";
	private static final String namersDirectoryName	 	= "silver-namer";
	private static final String sourceCodesSizeName 	= "sourceCodesSize";
	private static final String sourceCodesCountName 	= "sourceCodesCount";
	private static final String sourceCodeDirectoryName	= "source-code";
	
	protected final String folderPath;
	
	public StatusStorageResource(String folderPath) {
		this.folderPath = folderPath;
	}
	
	@GET
	@Timed
	public Response get() {
		try {
			StringBuilder sb = new StringBuilder();
			
			collectMachineInfo(sb);
			collectDataInfo(sb);
			
			return Response.ok(sb.toString()).build();
		} catch (Exception e) {
			logger.error("Failed retrieving System Status", e);
			return Response.serverError().entity("Failed retrieving System Status").build();
		}
	}
	
	private void collectDataInfo(StringBuilder sb) {
		File directory = new File(folderPath);
		Map<String, Long> mappedData = traverseTreeForData(directory);
		
		StatusUtil.appendInfoMessage(sb, "------------------ Data Info ------------------", "");
		StatusUtil.appendInfoMessage(sb, hitsSizeName + ": ", StatusUtil.bytesToKbString(mappedData.get(hitsSizeName)));
		StatusUtil.appendInfoMessage(sb, hitsCountName + ": ", mappedData.get(hitsCountName));
		StatusUtil.appendInfoMessage(
				sb, namersSizeName + ": ", StatusUtil.bytesToKbString(mappedData.get(namersSizeName)));
		StatusUtil.appendInfoMessage(sb, namersCountName + ": ", mappedData.get(namersCountName));
		StatusUtil.appendInfoMessage(
				sb, sourceCodesSizeName + ": ", StatusUtil.bytesToKbString(mappedData.get(sourceCodesSizeName)));
		StatusUtil.appendInfoMessage(sb, sourceCodesCountName + ": ", mappedData.get(sourceCodesCountName));
		StatusUtil.appendInfoMessage(sb, "Total free space left: ", StatusUtil.bytesToMbString(directory.getFreeSpace()));
	}
	
	private Map<String, Long> traverseTreeForData(File directory) {
		Map<String, Long> map = initializeMapForData();
		
		if (directory.isDirectory()) {
			traverseTreeForData(directory, map);
		}
		
		return map;
	}
	
	// Traverse into the directory and it's subdirectories,
	// avoiding any hidden or visible files until finding
	// the suitable directory (hits/namers/source-code).
	private void traverseTreeForData(File directory, Map<String, Long> map) {
		for(File file : directory.listFiles()) {
			if (file.isHidden() || !file.isDirectory()) {
				continue;
			}
			
			directoryHandler(map, file);
		}
	}
	
	// Direct hits/namers/source-code directory to it's handler
	// or keep traversing.
	private void directoryHandler(Map<String, Long> map, File directory) {
		switch (directory.getName()) {
			case hitsDirectoryName : {
				handleSpecialDirectory(directory, hitsSizeName, hitsCountName, map);
				break;
			} 
			case namersDirectoryName : {
				handleSpecialDirectory(directory, namersSizeName, namersCountName, map);
				break;
			} 
			case sourceCodeDirectoryName : {
				handleSpecialDirectory(directory, sourceCodesSizeName, sourceCodesCountName, map);
				break;
			} 
			default : {
				traverseTreeForData(directory, map);
			}
		}
	}
	
	// Extract data of visible files.
	private void handleSpecialDirectory(File directory, String sizeName, String countName, Map<String, Long> map) {
		for(File file : directory.listFiles()) {
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
		
		return result;
	}
	
	private void collectMachineInfo(StringBuilder sb) {
		StatusUtil.appendInfoMessage(sb, "----------------- Machine Info ----------------", "");
		StatusUtil.appendInfoMessage(sb, "Machine Name: ", StatusUtil.getMachineName());
		StatusUtil.appendInfoMessage(sb, "PID: ", StatusUtil.getProcessId());
		StatusUtil.appendInfoMessage(sb, "JVM Uptime: ", StatusUtil.getJvmUpTime());
		StatusUtil.appendInfoMessage(sb, "Available processors: ", StatusUtil.getAvailableProcessors());
		StatusUtil.appendInfoMessage(sb, "Load Average: ", StatusUtil.getLoadAvg());
		StatusUtil.appendInfoMessage(sb, "Process CPU Load: ", StatusUtil.getProcessCpuLoad());
		StatusUtil.appendInfoMessage(sb, "RAM Total: ", StatusUtil.getRamTotal());
		StatusUtil.appendInfoMessage(sb, "RAM Used: ", StatusUtil.getRamUsed());
		StatusUtil.appendInfoMessage(sb, "Heap Size: ", StatusUtil.getHeapSize());
		StatusUtil.appendInfoMessage(sb, "Perm Gen Size: ", StatusUtil.getPermGenSize());
	}
}
