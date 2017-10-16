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
	
	public static InMemoryCache create(int maxCacheSize, String cacheLogLevel) {
		int minAllowedCacheSize = 65536;
		int maxAllowedCacheSize = 134217728;
		
		if (maxCacheSize < minAllowedCacheSize) {
			logger.warn("Minimum allowable In Memory Cache size = ", minAllowedCacheSize);
			maxCacheSize = minAllowedCacheSize;
		}
		else if (maxCacheSize > maxAllowedCacheSize) {
			logger.warn("Maximum allowable In Memory Cache size = ", maxAllowedCacheSize);
			maxCacheSize = maxAllowedCacheSize;
		}
		
		return new InMemoryCache(maxCacheSize, cacheLogLevel);
	}
	
	private InMemoryCache(final int maxCacheSize, final String cacheLogLevel) {

		logger.info("In Memory Cache maximum size = " + maxCacheSize);
		
		int estimatedSizePerElement = 600;
		int initialCapacity = (int)Math.pow(2, Math.ceil(Math.log((float)maxCacheSize / estimatedSizePerElement) / Math.log(2)));
		
		cache = Collections.synchronizedMap(
				
				new LinkedHashMap<String, String>(initialCapacity,0.75f, true) {
					
					private int cacheSize = 0;
					
					@Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
						boolean remove = cacheSize > maxCacheSize;
						if (remove) {
							cacheSize -= (eldest.getKey().length() + eldest.getValue().length());
							String logMsg = "InMemoryCache max size exceeded. Count = " + cache.size() + ". Size = " + cacheSize;
							switch (cacheLogLevel) {
								case "info": logger.info(logMsg); break;
								default: logger.debug(logMsg);
							}
						}
						return remove;
					}
					
					@Override public String put(String key, String value) {
						cacheSize += (key.length() + value.length());
						String logMsg = "InMemoryCache element inserted. Count = " + cache.size() + ". Size = " + cacheSize;
						switch (cacheLogLevel) {
							case "info": logger.info(logMsg); break;
							default: logger.debug(logMsg);
						}
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
