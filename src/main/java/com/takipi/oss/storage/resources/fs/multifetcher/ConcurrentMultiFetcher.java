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
	private static final int MAX_THREADS = 30;
	
	private final ExecutorService executorService;
	private final AtomicInteger threadCount = new AtomicInteger();
	
	public ConcurrentMultiFetcher() {
		
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
		
		executorService = Executors.newFixedThreadPool(MAX_THREADS, threadFactory);
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
		
		for (final RecordWithData recordWithData : recordsWithData) {
			if (recordWithData.getData() == null) {
				Callable<String> callable = new Callable<String>()
				{
					@Override
					public String call() throws Exception
					{
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
		
		logger.info("---------- Concurrent multi fetch request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
		
		return new MultiFetchResponse(recordsWithData);
	}
}