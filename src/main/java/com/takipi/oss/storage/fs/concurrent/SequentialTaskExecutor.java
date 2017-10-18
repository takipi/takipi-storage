package com.takipi.oss.storage.fs.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SequentialTaskExecutor implements TaskExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(SequentialTaskExecutor.class);
	
	@Override
	public void execute(List<Runnable> tasks) {
		
		final int count = tasks.size();
		
		SimpleStopWatch stopWatch = new SimpleStopWatch();
		
		logger.debug("Starting sequential execute for {} tasks", count);
		
		for (Runnable task : tasks) {
			try {
				task.run();
			}
			catch (Exception e) {
				logger.error("Error running task", e);
			}
		}
		
		logger.debug("Sequential task executor executed {} tasks in {} ms", count, stopWatch.elapsed());
	}
}
