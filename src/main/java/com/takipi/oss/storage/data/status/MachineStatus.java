package com.takipi.oss.storage.data.status;

public class MachineStatus
{
	private long hitsCount;
	private long hitsSizeBytes;
	private long namersCount;
	private long namersSizeBytes;
	private long sourceCodeCount;
	private long sourceCodeSizeBytes;
	private long cerebroCount;
	private long cerebroSizeBytes;
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
	
	public void setHitsCount(long hitsCount) {
		this.hitsCount = hitsCount;
	}
	
	public long getHitsCount() {
		return hitsCount;
	}
	
	public void setHitsSizeBytes(long hitsSizeBytes) {
		this.hitsSizeBytes = hitsSizeBytes;
	}
	
	public long getHitsSizeBytes() {
		return hitsSizeBytes;
	}
	
	public void setNamersCount(long namersCount) {
		this.namersCount = namersCount;
	}
	
	public long getNamersCount() {
		return namersCount;
	}
	
	public void setNamersSizeBytes(long namersSizeBytes) {
		this.namersSizeBytes = namersSizeBytes;
	}
	
	public long getNamersSizeBytes() {
		return namersSizeBytes;
	}
	
	public void setSourceCodeCount(long sourceCodeCount) {
		this.sourceCodeCount = sourceCodeCount;
	}
	
	public long getSourceCodeCount() {
		return sourceCodeCount;
	}
	
	public void setSourceCodeSizeBytes(long sourceCodeSizeBytes) {
		this.sourceCodeSizeBytes = sourceCodeSizeBytes;
	}
	
	public long getSourceCodeSizeBytes() {
		return sourceCodeSizeBytes;
	}
	
	public void setCerebroCount(long cerebroCount) {
		this.cerebroCount = cerebroCount;
	}
	
	public long getCerebroCount() {
		return cerebroCount;
	}
	
	public void setCerebroSizeBytes(long cerebroSizeBytes) {
		this.cerebroSizeBytes = cerebroSizeBytes;
	}
	
	public long getCerebroSizeBytes() {
		return cerebroSizeBytes;
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
