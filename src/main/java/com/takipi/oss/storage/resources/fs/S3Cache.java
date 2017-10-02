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
	public String get(String key)
	{
		return null;
	}
	
	@Override
	public String put(String key, String value)
	{
		return null;
	}
}

class S3CacheInMemory implements S3Cache {

	private static final int MAX_CACHE_SIZE = 4194304;  // 4 MB
	
	private final Map<String, String> cache;
	
	S3CacheInMemory() {

		cache = Collections.synchronizedMap(
				new LinkedHashMap<String, String>(1024,0.75f, true) {
					
					private int cacheSize = 0;
					
					@Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
						boolean remove = cacheSize > MAX_CACHE_SIZE;
						if (remove) {
							cacheSize -= eldest.getValue().length();
						}
						return remove;
					}
					
					@Override public String put(String key, String value) {
						cacheSize += value.length();
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
