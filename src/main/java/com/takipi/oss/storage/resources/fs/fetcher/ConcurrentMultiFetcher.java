package com.takipi.oss.storage.resources.fs.fetcher;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.cache.Cache;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentMultiFetcher implements MultiFetcher {
	
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentMultiFetcher.class);
	
	private static final int MAX_THREADS = 50;
	
	private final ExecutorService executorService;
	
	private final AtomicInteger threadCount = new AtomicInteger();
	
	public ConcurrentMultiFetcher() {

		executorService = Executors.newFixedThreadPool(MAX_THREADS, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setName("fetcher_thread_" + threadCount.incrementAndGet());
				return t;
			}
		});
	}
	
	@Override
	public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem, Cache cache) {
		final EncodingType encodingType = request.encodingType;
		final List<Record> recordsToRetrieve = request.records;
		final int count = recordsToRetrieve.size();
		final List<Future<String>> futures = new ArrayList<>(count);
		final List<RecordWithData> recordsWithData = new ArrayList<>(count);
		
		logger.debug("---------- Starting concurrent multi fetch request for " + count + " records");
		
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		
		for (Record record : recordsToRetrieve) {
			String value = cache.get(record.getKey());
			recordsWithData.add(RecordWithData.of(record, value));
			if (value != null) {
				logger.debug("Object for key " + record.getKey() + " found in cache. " + value.length() + " bytes");
			}
			else {
				Callable<String> callable = new FetcherCallable(filesystem, record, encodingType);
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
	
	private static class FetcherCallable implements Callable<String> {
		
		static AtomicInteger counter = new AtomicInteger();
		final Filesystem<Record> filesystem;
		final Record record;
		final EncodingType encodingType;
		
		FetcherCallable(Filesystem<Record> filesystem, Record record, EncodingType encodingType) {
			this.filesystem = filesystem;
			this.record = record;
			this.encodingType = encodingType;
		}
		
		@Override
		public String call() throws Exception {
			SimpleStopWatch stopWatch = new SimpleStopWatch();
			String value = null;
			final int MAX_TRIES = 2;
			int count = 0;
			
			while ((value == null) && (count < MAX_TRIES)) {
				
				if (count++ > 0) {
					logger.warn("Retry loading object for key " + record.getKey());
					stopWatch.reset();
				}
				
				try {
					value = FilesystemUtil.read(filesystem, record, encodingType);
				}
				catch (Exception e) {
					// Need this catch because some exceptions inside FilesystemUtil.read are caught and result in a
					// null return value, and some are thrown. The code would be simpler if all exceptions were thrown. 
				}
			}
			
			if (value != null) {
				
				logger.debug("--------------------- " + Thread.currentThread().getName() + " loaded key " +
							record.getKey() + " in " + stopWatch.elapsed() + " ms. " + value.length() + " bytes");
				
				return value;
			}
			else {
				
				logger.error("Failed to load object for key: " + record.getKey() + ". Elapsed time = " + stopWatch.elapsed() + " ms");
				
				throw new RuntimeException("Failed to load object for key: " + record.getKey());
			}
		}
	}
}