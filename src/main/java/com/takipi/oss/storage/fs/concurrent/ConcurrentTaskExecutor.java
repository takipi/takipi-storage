package com.takipi.oss.storage.fs.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTaskExecutor implements TaskExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentTaskExecutor.class);
	
	private final ExecutorService executorService;
	private final AtomicInteger threadCount = new AtomicInteger();
	
	public ConcurrentTaskExecutor(int maxThreads) {
		
		if (maxThreads > 50) {
			logger.warn("ConcurrentTaskExecutor cannot have more than 50 threads");
			maxThreads = 50;
		}
		else if (maxThreads < 2) {
			logger.warn("ConcurrentTaskExecutor cannot have less than 2 threads");
			maxThreads = 2;
		}
		
		logger.info("ConcurrentTaskExecutor maximum number of threads = " + maxThreads);
		
		ThreadFactory threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setName("conctaskexec_thread_" + threadCount.incrementAndGet());
				return t;
			}
		};
		
		executorService = Executors.newFixedThreadPool(maxThreads, threadFactory);
	}
	
	@Override
	public void execute(List<Task> tasks) {
		
		final int count = tasks.size();
		
		logger.debug("---------- Starting concurrent task execute for " + count + " tasks");
		
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		
		if (count == 1) {
			try {
				tasks.get(0).getRunnable().run();
			}
			catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		else {
			
			final List<Future<?>> futures = new ArrayList<>(count);
			
			for (Task command : tasks) {
				futures.add(executorService.submit(command.getRunnable()));
			}
			
			for (Future<?> future : futures) {
				try {
					future.get(1, TimeUnit.MINUTES);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		logger.debug("---------- Concurrent task executor executed " + count + "tasks in " + stopWatch.elapsed() + " ms");
	}
}
