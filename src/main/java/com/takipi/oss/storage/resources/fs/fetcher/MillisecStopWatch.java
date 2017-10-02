package com.takipi.oss.storage.resources.fs.fetcher;

class MillisecStopWatch
{
	private long start;
	
	MillisecStopWatch() {
		reset();
	}
	
	long elapsed() {
		return System.currentTimeMillis() - start;
	}
	
	void reset() {
		start = System.currentTimeMillis();
	}
}
