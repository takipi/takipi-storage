package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.fs.concurrent.ConcurrentTaskExecutor;

public class ConcurrentMultiFetcher extends BaseMultiFetcher {
	
	public ConcurrentMultiFetcher(int maxThreads) {
		super(new ConcurrentTaskExecutor(maxThreads));
	}
}
