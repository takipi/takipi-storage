package com.takipi.oss.storage.data.status;

import java.util.Hashtable;
import java.util.Map;

public class MachineStatus
{
	private Map<String, Long> countMap = new Hashtable<>();
	private Map<String, Long> sizeByBytesMap = new Hashtable<>();
	
	private long freeSpaceLeftBytes;
	private String machineName;
	private String pid;
	private long jvmUpTimeMillis;
	private long availableProcessors;
	private double loadAverage;
	private double processCpuLoad;
	private long totalRamBytes;
	private long usedRamBytes;
	private long heapSizeBytes;
	private long permGenSizeBytes;
	private String version;
	
	public void setObjectCount(String name, long count)
	{
		this.countMap.put(name, count);
	}
	
	public void setObjectSizeBytes(String name, long sizeBytes) {
		this.sizeByBytesMap.put(name, sizeBytes);
	}
	
	public void setCountMap(Map<String, Long> map) {
		this.countMap = map;
	}
	
	public Map<String, Long> getCountMap() {
		return countMap;
	}
	
	public void setSizeByBytesMap(Map<String, Long> map) {
		this.sizeByBytesMap = map;
	}
	
	public Map<String, Long> getSizeByBytesMap() {
		return sizeByBytesMap;
	}
	
	public void setFreeSpaceLeftBytes(long freeSpaceLeftBytes) {
		this.freeSpaceLeftBytes = freeSpaceLeftBytes;
	}
	
	public long getFreeSpaceLeftBytes() {
		return freeSpaceLeftBytes;
	}
	
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getMachineName() {
		return machineName;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public String getPid() {
		return pid;
	}
	
	public void setJvmUpTimeMillis(long jvmUpTimeMillis) {
		this.jvmUpTimeMillis = jvmUpTimeMillis;
	}
	
	public long getJvmUpTimeMillis() {
		return jvmUpTimeMillis;
	}
	
	public void setAvailableProcessors(long availableProcessors) {
		this.availableProcessors = availableProcessors;
	}
	
	public long getAvailableProcessors() {
		return availableProcessors;
	}
	
	public void setLoadAverage(double loadAverage) {
		this.loadAverage = loadAverage;
	}
	
	public double getLoadAverage() {
		return loadAverage;
	}
	
	public void setProcessCpuLoad(double processCpuLoad) {
		this.processCpuLoad = processCpuLoad;
	}
	
	public double getProcessCpuLoad() {
		return processCpuLoad;
	}
	
	public void setTotalRamBytes(long totalRamBytes) {
		this.totalRamBytes = totalRamBytes;
	}
	
	public long getTotalRamBytes() {
		return totalRamBytes;
	}
	
	public void setUsedRamBytes(long usedRamBytes) {
		this.usedRamBytes = usedRamBytes;
	}
	
	public long getUsedRamBytes() {
		return usedRamBytes;
	}
	
	public void setHeapSizeBytes(long heapSizeBytes) {
		this.heapSizeBytes = heapSizeBytes;
	}
	
	public long getHeapSizeBytes() {
		return heapSizeBytes;
	}
	
	public void setPermGenSizeBytes(long permGenSizeBytes) {
		this.permGenSizeBytes = permGenSizeBytes;
	}
	
	public long getPermGenSizeBytes() {
		return permGenSizeBytes;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}
