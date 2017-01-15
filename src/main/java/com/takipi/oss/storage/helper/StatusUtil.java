package com.takipi.oss.storage.helper;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;

import com.google.common.base.Strings;
import com.sun.management.OperatingSystemMXBean;

public class StatusUtil {
	private static final String permGenStringJava7 	= "PS Perm Gen";
	private static final String permGenStringJava8 	= "Metaspace";
	private static final String defaultFailureStringValue = "N/A";
	private static final long defaultFailureNumberValue = -1l;
	private static String processName;
	private static String processId;
	private static String machineName;
	private static String machineVersion;
	
	public static String getMachineName() {
		if (Strings.isNullOrEmpty(machineName)) {
			machineName = getName(getProcessName());
		}
		
		return machineName;
	}
	
	public static String getMachineVersion() {
		if (Strings.isNullOrEmpty(machineVersion)) {
			try {
				machineVersion = StatusUtil.class.getPackage().getImplementationVersion();
			}
			catch (Exception e) { }
			
			if (Strings.isNullOrEmpty(machineVersion)) {
			    machineVersion = "N/A";
			}
		}
		
		return machineVersion;
	}
	
	public static String getProcessName() {
		if (Strings.isNullOrEmpty(processName)) {
			RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
			processName = rmxb.getName();
		}
		
		return processName;
	}
	
	public static String getProcessId() {
		if (Strings.isNullOrEmpty(processId)) {
			processId = getPID(getProcessName());
		}
		
		return processId;
	}
	
	public static long getJvmUpTimeInMilli() {
		RuntimeMXBean rmxb = getRMXB();
		
		if (rmxb != null) {
			return rmxb.getUptime();
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	public static long getAvailableProcessors() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return osmxb.getAvailableProcessors();
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	public static double getLoadAvg() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			double loadAvg = osmxb.getSystemLoadAverage();
			
			return (loadAvg > 0) ? precisionOfTwo(loadAvg) : defaultFailureNumberValue;
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	public static double getProcessCpuLoad() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return precisionOfTwo(osmxb.getProcessCpuLoad());
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	public static long getTotalRamInBytes() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return osmxb.getTotalPhysicalMemorySize();
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	public static long getUsedRamInBytes() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize();
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	public static long getHeapSizeInBytes() {
		Runtime runtime = Runtime.getRuntime();
		
		return runtime.totalMemory() - runtime.freeMemory();
	}
	
	public static long getPermGenSizeInBytes() {
		MemoryUsage memUsage = getPermGenMemoryUsage();
		
		if (memUsage != null) {
			return memUsage.getUsed();
		} else {
			return defaultFailureNumberValue;
		}
	}
	
	private static OperatingSystemMXBean getOSMXB() {
		try {
			return (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		} catch (Exception e) {
			return null;
		}
	}
	
	private static RuntimeMXBean getRMXB() {
		try {
			return ManagementFactory.getRuntimeMXBean();
		} catch (Exception e) {
			return null;
		}
	}
	
	private static MemoryUsage getPermGenMemoryUsage() {
		for (MemoryPoolMXBean mx : ManagementFactory.getMemoryPoolMXBeans()) {
			if ((mx.getName().equalsIgnoreCase(permGenStringJava7)) ||
				(mx.getName().equalsIgnoreCase(permGenStringJava8))) {
				return mx.getUsage();
			}
		}
		
		return null;
	}
	
	private static String getName(String processName) {
		try {
			return processName.split("@")[1];
		} catch (Exception e) {
			return defaultFailureStringValue;
		}
	}
	
	private static String getPID(String processName) {
		try {
			return processName.split("@")[0];
		} catch (Exception e) {
			return defaultFailureStringValue;
		}
	}
	
	private static double precisionOfTwo(double value) {
		return Math.floor(value * 100) / 100;
	}
	
	public static void appendInfoMessage(StringBuilder sb, String header, String value) {
		sb.append(header);
		
		if (!Strings.isNullOrEmpty(value)) {
			sb.append(value);
		}
		
		sb.append("\n");
	}
	
	public static void appendInfoMessage(StringBuilder sb, String header, long value) {
		appendInfoMessage(sb, header, String.valueOf(value));
	}
}
