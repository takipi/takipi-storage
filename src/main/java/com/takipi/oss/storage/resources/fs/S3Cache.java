package com.takipi.oss.storage.resources.fs;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

interface S3Cache {
	String get(String key);
	String put(String key, String value);
}

class DummyS3Cache implements S3Cache {
	
	@Override
	public String get(String key) {
		return null;
	}
	
	@Override
	public String put(String key, String value) {
		return null;
	}
}

class S3CacheInMemory implements S3Cache {

	private final Map<String, String> cache;
	
	S3CacheInMemory(final int maxCacheSize) {
		
		int estimatedSizePerElement = 600;
		int initialCapacity = (int)Math.pow(2, Math.ceil(Math.log((float)maxCacheSize / estimatedSizePerElement) / Math.log(2)));
		
		cache = Collections.synchronizedMap(
				
				new LinkedHashMap<String, String>(initialCapacity,0.75f, true) {
					
					private int cacheSize = 0;
					
					@Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
						boolean remove = cacheSize > maxCacheSize;
						if (remove) {
							cacheSize -= (eldest.getKey().length() + eldest.getValue().length());
						}
						return remove;
					}
					
					@Override public String put(String key, String value) {
						cacheSize += (key.length() + value.length());
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
