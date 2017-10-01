package com.takipi.oss.storage.resources.fs;

class StopWatch
{
	private long start;
	
	StopWatch() {
		reset();
	}
	
	long elapsed() {
		return System.currentTimeMillis() - start;
	}
	
	void reset() {
		start = System.currentTimeMillis();
	}
}
