package com.takipi.oss.storage.resources.fs.fetcher;

class SimpleStopWatch
{
	private long start;
	
	SimpleStopWatch() {
		reset();
	}
	
	long elapsed() {
		return System.currentTimeMillis() - start;
	}
	
	void reset() {
		start = System.currentTimeMillis();
	}
}
