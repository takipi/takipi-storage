package com.takipi.oss.storage.fs.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryCache implements Cache
{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);
	
	private final Map<String, String> cache;
	
	public InMemoryCache(int maxCacheSize) {
		
		int oneMegaByte = 1048576;
		int hundredMegaByte = 100 * 1048576;
		
		if (maxCacheSize < oneMegaByte) {
			logger.warn("Minimum In Memory Cache size = 1048576");
			maxCacheSize = oneMegaByte;
		}
		else if (maxCacheSize > hundredMegaByte) {
			logger.warn("Maximum allowable In Memory Cache size = ", hundredMegaByte);
			maxCacheSize = hundredMegaByte;
		}
		
		final int cacheSizeLimit = maxCacheSize;
		logger.info("In Memory Cache maximum size = " + maxCacheSize);
		
		int estimatedSizePerElement = 600;
		int initialCapacity = (int)Math.pow(2, Math.ceil(Math.log((float)maxCacheSize / estimatedSizePerElement) / Math.log(2)));
		
		cache = Collections.synchronizedMap(
				
				new LinkedHashMap<String, String>(initialCapacity,0.75f, true) {
					
					private int cacheSize = 0;
					
					@Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
						boolean remove = cacheSize > cacheSizeLimit;
						if (remove) {
							cacheSize -= (eldest.getKey().length() + eldest.getValue().length());
						}
						return remove;
					}
					
					@Override public String put(String key, String value) {
						cacheSize += (key.length() + value.length());
						logger.debug("InMemoryCache size = " + cacheSize);
						return super.put(key, value);
					}
				});
		
	}
	
	@Override
	public String get(String key) {
		return cache.get(key);
	}
	
	@Override
	public String put(String key, String value) {
		return cache.put(key, value);
	}
}
