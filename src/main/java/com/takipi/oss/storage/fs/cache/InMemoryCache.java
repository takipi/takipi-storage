package com.takipi.oss.storage.fs.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class InMemoryCache implements Cache
{
	private final Map<String, String> cache;
	
	public InMemoryCache(final int maxCacheSize) {
		
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
