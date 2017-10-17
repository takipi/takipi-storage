package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.fs.concurrent.SequentialTaskExecutor;

public class SequentialMultiFetcher extends BaseMultiFetcher {
	
	public SequentialMultiFetcher() {
		super(new SequentialTaskExecutor());
	}
}
