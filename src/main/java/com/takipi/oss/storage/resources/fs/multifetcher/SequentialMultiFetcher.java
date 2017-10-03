package com.takipi.oss.storage.resources.fs.multifetcher;

import com.google.common.collect.Lists;
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
		
		List<RecordWithData> records = Lists.newArrayList();
		final int count = request.records.size();
		final EncodingType encodingType = request.encodingType;
		logger.info("---------- Starting sequential multi fetch request for " + count + " records");
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		Cache cache = filesystem.getCache();
		
		for (Record record : request.records) {
			
			String value = cache.get(record.getKey());
			
			if (value != null) {
				logger.debug("Object for key " + record.getKey() + " found in cache. " + value.length() + " bytes");
				records.add(RecordWithData.of(record, value));
			}
			else {
				try {
					value = load(filesystem, record, encodingType);
					records.add(RecordWithData.of(record, value));
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		logger.info("---------- Sequential multi fetch request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
		
		return new MultiFetchResponse(records);
	}
}
