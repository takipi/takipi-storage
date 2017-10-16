package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;

public interface MultiFetcher {
	MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem);
}
