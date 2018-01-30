package com.takipi.oss.storage.resources.diag;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.TakipiStorageConfiguration;
import com.takipi.oss.storage.data.status.MachineStatus;
import com.takipi.oss.storage.helper.StatusUtil;

import java.util.Collections;
import java.util.Hashtable;

@Path("/storage/v1/diag/status")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class StatusStorageResource {
	private static Map<String, String> myMap;
	static {
		Map<String, String> aMap = new Hashtable<>();
		aMap.put("hits","hits");
		aMap.put("namers","silver-namer");
		aMap.put("sources","source-code");
		aMap.put("cerebro","cerebro");
		aMap.put("overmind","overmind");
		aMap.put("whitetigers","white-tiger");
		myMap = Collections.unmodifiableMap(aMap);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(StatusStorageResource.class);
	
	protected final String folderPath;
	
	public StatusStorageResource(TakipiStorageConfiguration configuration) {
		this.folderPath = configuration.getFolderPath();
	}
	
	@POST
	@Timed
	public Response post() {
		try {
			MachineStatus machineStatus = new MachineStatus();
			
			collectMachineInfo(machineStatus);
			collectDataInfo(machineStatus);
			
			return Response.ok(machineStatus).build();
		} catch (Exception e) {
			logger.error("Failed retrieving System Status", e);
			return Response.serverError().entity("Failed retrieving System Status").build();
		}
	}
	
	private void collectDataInfo(MachineStatus machineStatus) {
		File directory = new File(folderPath);
		Map<String, Long> mappedData = traverseTreeForData(directory);
		
		for (String key : myMap.keySet())
		{
			machineStatus.setObjectSizeBytes(key, mappedData.get(getSizeName(key)));
			machineStatus.setObjectCount(key, mappedData.get(getCountName(key)));
		}
		
		machineStatus.setFreeSpaceLeftBytes(directory.getFreeSpace());
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
		
		for (String key : myMap.keySet())
		{
			if (myMap.get(key).equals(directory.getName()))
			{
				handleSpecialDirectory(directory, getSizeName(key), getCountName(key), map);
				return;
			}
		}
		traverseTreeForData(directory, map);
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
		for (String key : myMap.keySet()) {
			result.put(getSizeName(key), 0l);
			result.put(getCountName(key), 0l);
		}
		
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
	
	private String getSizeName(String objectName) {
		return objectName + " size";
	}
	
	private String getCountName(String objectName) {
		return objectName + " count";
	}
}
