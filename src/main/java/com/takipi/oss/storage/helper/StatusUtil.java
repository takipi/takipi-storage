package com.takipi.oss.storage.helper;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;

public class StatusUtil {
  private static final String permGenStringJava7 = "PS Perm Gen";
  
  private static final String permGenStringJava8 = "Metaspace";
  
  private static final String defaultFailureStringValue = "N/A";
  
  private static final long defaultFailureNumberValue = -1L;
  
  private static String processName;
  
  private static String processId;
  
  private static String machineName;
  
  private static String machineVersion;
  
  public static String getMachineName() {
    if (StringUtil.isNullOrEmpty(machineName))
      machineName = getName(getProcessName()); 
    return machineName;
  }
  
  public static String getMachineVersion() {
    if (StringUtil.isNullOrEmpty(machineVersion)) {
      try {
        machineVersion = Object.class.getPackage().getImplementationVersion();
      } catch (Exception exception) {}
      if (StringUtil.isNullOrEmpty(machineVersion))
        machineVersion = "N/A"; 
    } 
    return machineVersion;
  }
  
  public static String getProcessName() {
    if (StringUtil.isNullOrEmpty(processName)) {
      RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
      processName = rmxb.getName();
    } 
    return processName;
  }
  
  public static String getProcessId() {
    if (StringUtil.isNullOrEmpty(processId))
      processId = getPID(getProcessName()); 
    return processId;
  }
  
  public static long getJvmUpTimeInMilli() {
    RuntimeMXBean rmxb = getRMXB();
    if (rmxb != null)
      return rmxb.getUptime(); 
    return -1L;
  }
  
  public static long getAvailableProcessors() {
    OperatingSystemMXBean osmxb = getOSMXB();
    if (osmxb != null)
      return osmxb.getAvailableProcessors(); 
    return -1L;
  }
  
  public static double getLoadAvg() {
    OperatingSystemMXBean osmxb = getOSMXB();
    if (osmxb != null) {
      double loadAvg = osmxb.getSystemLoadAverage();
      return (loadAvg > 0.0D) ? precisionOfTwo(loadAvg) : -1.0D;
    } 
    return -1.0D;
  }
  
  public static double getProcessCpuLoad() {
    OperatingSystemMXBean osmxb = getOSMXB();
    if (osmxb != null)
      return precisionOfTwo(osmxb.getProcessCpuLoad()); 
    return -1.0D;
  }
  
  public static long getTotalRamInBytes() {
    OperatingSystemMXBean osmxb = getOSMXB();
    if (osmxb != null)
      return osmxb.getTotalPhysicalMemorySize(); 
    return -1L;
  }
  
  public static long getUsedRamInBytes() {
    OperatingSystemMXBean osmxb = getOSMXB();
    if (osmxb != null)
      return osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize(); 
    return -1L;
  }
  
  public static long getHeapSizeInBytes() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() - runtime.freeMemory();
  }
  
  public static long getPermGenSizeInBytes() {
    MemoryUsage memUsage = getPermGenMemoryUsage();
    if (memUsage != null)
      return memUsage.getUsed(); 
    return -1L;
  }
  
  private static OperatingSystemMXBean getOSMXB() {
    try {
      return (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
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
      if (mx.getName().equalsIgnoreCase("PS Perm Gen") || mx
        .getName().equalsIgnoreCase("Metaspace"))
        return mx.getUsage(); 
    } 
    return null;
  }
  
  private static String getName(String processName) {
    try {
      return processName.split("@")[1];
    } catch (Exception e) {
      return "N/A";
    } 
  }
  
  private static String getPID(String processName) {
    try {
      return processName.split("@")[0];
    } catch (Exception e) {
      return "N/A";
    } 
  }
  
  private static double precisionOfTwo(double value) {
    return Math.floor(value * 100.0D) / 100.0D;
  }
  
  public static void appendInfoMessage(StringBuilder sb, String header, String value) {
    sb.append(header);
    if (!StringUtil.isNullOrEmpty(value))
      sb.append(value); 
    sb.append("\n");
  }
  
  public static void appendInfoMessage(StringBuilder sb, String header, long value) {
    appendInfoMessage(sb, header, String.valueOf(value));
  }
}

