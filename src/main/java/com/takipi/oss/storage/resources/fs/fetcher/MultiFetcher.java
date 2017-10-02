package com.takipi.oss.storage.resources.fs.fetcher;

import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.cache.Cache;

public interface MultiFetcher {
	
	MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem, Cache cache);
}
