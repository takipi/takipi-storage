package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.cache.Cache;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface MultiFetcher {
	
	MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem, Cache cache);
}

abstract class BaseMultiFetcher implements MultiFetcher {
	
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentMultiFetcher.class);
	
	static String load(Filesystem<Record> filesystem, Record record, EncodingType encodingType) {
		
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
			
			logger.info("--------------------- " + Thread.currentThread().getName() + " loaded key " +
					record.getKey() + " in " + stopWatch.elapsed() + " ms. " + value.length() + " bytes");
			
			return value;
		}
		else {
			
			logger.error("Failed to load object for key: " + record.getKey() + ". Elapsed time = " + stopWatch.elapsed() + " ms");
			
			throw new RuntimeException("Failed to load object for key: " + record.getKey());
		}
	}
}
