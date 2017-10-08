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
	
	private final ExecutorService executorService;
	private final AtomicInteger threadCount = new AtomicInteger();
	
	public ConcurrentMultiFetcher(int maxThreads) {
		
		if (maxThreads > 50) {
			logger.warn("ConcurrentMultiFetcher concurrency level can not be greater than 50");
			maxThreads = 50;
		}
		else if (maxThreads < 1) {
			logger.warn("ConcurrentMultiFetcher concurrency level can not be less than 1");
			maxThreads = 1;
		}
		
		logger.info("ConcurrentMultiFetcher maximum number of threads = " + maxThreads);
		
		ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setName("fetcher_thread_" + threadCount.incrementAndGet());
				return t;
			}
		};
		
		executorService = Executors.newFixedThreadPool(maxThreads, threadFactory);
	}
	
	@Override
	public MultiFetchResponse loadData(final MultiFetchRequest request, final Filesystem<Record> filesystem) {
		
		final EncodingType encodingType = request.encodingType;
		final List<Record> recordsToRetrieve = request.records;
		final int count = recordsToRetrieve.size();
		final List<Future<String>> futures = new ArrayList<>(count);
		Cache cache = filesystem.getCache();
		
		logger.info("---------- Starting concurrent multi fetch request for " + count + " records");
		
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		
		final List<RecordWithData> recordsWithData = loadFromCache(request.records, cache);
		
		// if only 1 record, then no need to initiate a multi-threaded load
		if (recordsWithData.size() == 1) {
			RecordWithData firstRecord = recordsWithData.get(0);
			if (firstRecord.getData() == null) {
				logger.debug("Only one record so loading object in calling thread");
				firstRecord.setData(load(filesystem, firstRecord.getRecord(), encodingType));
			}
		}
		else {
			for (final RecordWithData recordWithData : recordsWithData) {
				if (recordWithData.getData() == null) {
					Callable<String> callable = new Callable<String>() {
						@Override
						public String call() throws Exception {
							return load(filesystem, recordWithData.getRecord(), encodingType);
						}
					};
					futures.add(executorService.submit(callable));
				}
			}
			
			int futureIndex = 0;
			
			for (RecordWithData recordWithData : recordsWithData) {
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
		}
		
		logger.info("---------- Concurrent multi fetch request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
		
		return new MultiFetchResponse(recordsWithData);
	}
}