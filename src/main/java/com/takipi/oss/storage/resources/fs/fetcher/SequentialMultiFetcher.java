package com.takipi.oss.storage.resources.fs.fetcher;

import com.google.common.collect.Lists;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.cache.Cache;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SequentialMultiFetcher implements MultiFetcher {
	
	private static final Logger logger = LoggerFactory.getLogger(SequentialMultiFetcher.class);
	
	@Override
	public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem, Cache cache) {
		
		List<RecordWithData> records = Lists.newArrayList();
		final int count = request.records.size();
		
		logger.debug("---------- Starting sequential multi fetch request for " + count + " records");
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		
		for (Record record : request.records) {
			
			final String key = record.getKey();
			
			String value = cache.get(key);
			
			if (value != null) {
				logger.debug("Object for key " + record.getKey() + " found in cache. " + value.length() + " bytes");
				records.add(RecordWithData.of(record, value));
			}
			else {
				
				try {
					
					value = FilesystemUtil.read(filesystem, record, request.encodingType);
					
					if (value == null) {
						logger.warn("Key not found: {}", key);
					}
					else {
						records.add(RecordWithData.of(record, value));
						cache.put(key, value);
					}
				}
				catch (Exception e) {
					logger.error("Problem with record " + record, e);
				}
			}
		}
		
		logger.info("---------- Sequential multi fetch request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
		
		return new MultiFetchResponse(records);
	}
}
