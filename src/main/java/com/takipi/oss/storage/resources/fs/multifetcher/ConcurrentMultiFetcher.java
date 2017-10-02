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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentMultiFetcher extends BaseMultiFetcher {
	
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentMultiFetcher.class);
	private static final int MAX_THREADS = 50;
	
	private final ExecutorService executorService;
	private final AtomicInteger threadCount = new AtomicInteger();
	
	public ConcurrentMultiFetcher() {

		executorService = Executors.newFixedThreadPool(MAX_THREADS, r -> {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("fetcher_thread_" + threadCount.incrementAndGet());
			return t;
		});
	}
	
	@Override
	public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem, Cache cache) {
		
		final EncodingType encodingType = request.encodingType;
		final List<Record> recordsToRetrieve = request.records;
		final int count = recordsToRetrieve.size();
		final List<Future<String>> futures = new ArrayList<>(count);
		final List<RecordWithData> recordsWithData = new ArrayList<>(count);
		
		logger.info("---------- Starting concurrent multi fetch request for " + count + " records");
		
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		
		for (Record record : recordsToRetrieve) {
			String value = cache.get(record.getKey());
			recordsWithData.add(RecordWithData.of(record, value));
			if (value != null) {
				logger.info("Object for key " + record.getKey() + " found in cache. " + value.length() + " bytes");
			}
			else {
				
				Callable<String> callable = () -> load(filesystem, record, encodingType);
				futures.add(executorService.submit(callable));
			}
		}
		
		for (int i = 0, futureIndex = 0; i < count; ++i) {
			RecordWithData recordWithData = recordsWithData.get(i);
			if (recordWithData.getData() == null) {
				try {
					String value = futures.get(futureIndex++).get(20, TimeUnit.SECONDS);
					cache.put(recordWithData.getRecord().getKey(), value);
					recordWithData.setData(value);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		logger.info("---------- Concurrent multi fetch request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
		
		return new MultiFetchResponse(recordsWithData);
	}
}