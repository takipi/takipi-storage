package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SequentialMultiFetcher extends BaseMultiFetcher {
	
	private static final Logger logger = LoggerFactory.getLogger(SequentialMultiFetcher.class);
	
	@Override
	public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem) {
		
		final int count = request.records.size();
		logger.debug("---------- Starting sequential multi fetch request for " + count + " records");
		final EncodingType encodingType = request.encodingType;
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		Cache cache = filesystem.getCache();
		
		final List<RecordWithData> recordsWithData = loadFromCache(request.records, cache);
		
		for (RecordWithData recordWithData : recordsWithData) {
			
			if (recordWithData.getData() == null) {
				try {
					String value = load(filesystem, recordWithData.getRecord(), encodingType);
					recordWithData.setData(value);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		logger.debug("---------- Sequential multi fetch request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
		
		return new MultiFetchResponse(recordsWithData);
	}
}
