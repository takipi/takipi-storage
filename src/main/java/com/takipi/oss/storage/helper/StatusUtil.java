package com.takipi.oss.storage.helper;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.sql.Time;

import com.google.common.base.Strings;
import com.sun.management.OperatingSystemMXBean;

public class StatusUtil {
	private static final String permGenStringJava7 	= "PS Perm Gen";
	private static final String permGenStringJava8 	= "Metaspace";
	private static final String defaultFailureValue = "N/A";
	private static String processName;
	private static String processId;
	private static String machineName;
	
	public static String getMachineName() {
		if (Strings.isNullOrEmpty(machineName)) {
			machineName = getName(getProcessName());
		}
		
		return machineName;
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
	
	public static String getJvmUpTime() {
		RuntimeMXBean rmxb = getRMXB();
		
		if (rmxb != null) {
			return MilliToTime(rmxb.getUptime());
		} else {
			return defaultFailureValue;
		}
	}
	
	public static String getAvailableProcessors() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return String.valueOf(osmxb.getAvailableProcessors());
		} else {
			return defaultFailureValue;
		}
	}
	
	public static String getLoadAvg() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			double loadAvg = osmxb.getSystemLoadAverage();
			
			return loadAvg > 0 ? percisionOfTwo(loadAvg) : defaultFailureValue;
		} else {
			return defaultFailureValue;
		}
	}
	
	public static String getProcessCpuLoad() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return percisionOfTwo(osmxb.getProcessCpuLoad());
		} else {
			return defaultFailureValue;
		}
	}
	
	public static String getRamTotal() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return bytesToMbString(osmxb.getTotalPhysicalMemorySize());
		} else {
			return defaultFailureValue;
		}
	}
	
	public static String getRamUsed() {
		OperatingSystemMXBean osmxb = getOSMXB();
		
		if (osmxb != null) {
			return bytesToMbString(osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize());
		} else {
			return defaultFailureValue;
		}
	}
	
	public static String getHeapSize() {
		Runtime runtime = Runtime.getRuntime();
		
		return bytesToMbString(runtime.totalMemory() - runtime.freeMemory());
	}
	
	public static String getPermGenSize() {
		MemoryUsage memUsage = getPermGenMemoryUsage();
		
		if (memUsage != null) {
			return bytesToMbString(memUsage.getUsed());
		} else {
			return defaultFailureValue;
		}
	}
	
	private static com.sun.management.OperatingSystemMXBean getOSMXB() {
		try {
			return (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
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
			return defaultFailureValue;
		}
	}
	
	private static String getPID(String processName) {
		try {
			return processName.split("@")[0];
		} catch (Exception e) {
			return defaultFailureValue;
		}
	}
	
	private static String MilliToTime(long time) {
		try {
			return new Time(time).toString();
		} catch (Exception e) {
			return defaultFailureValue;
		}
	}
	
	private static String bytesConversion(long bytes, int amount) {
		if (bytes == 0) {
			return "0";
		}
		
		double result = bytes;
		
		for (int i = 0; i < amount; i++) {
			result = result / 1024;
		}
		
		return percisionOfTwo(result);
	}
	
	private static String percisionOfTwo(double value) {
		return String.valueOf(Math.floor(value * 100) / 100);
	}
	
	public static String bytesToMbString(long bytes) {
		return bytesConversion(bytes, 2) + "Mb";
	}
	
	public static String bytesToKbString(long bytes) {
		return bytesConversion(bytes, 1) + "Kb";
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
