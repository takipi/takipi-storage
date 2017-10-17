package com.takipi.oss.storage.fs.concurrent;

public class SimpleStopWatch
{
	private long start;
	
	public SimpleStopWatch() {
		reset();
	}
	
	public long elapsed() {
		return System.currentTimeMillis() - start;
	}
	
	public void reset() {
		start = System.currentTimeMillis();
	}
}
