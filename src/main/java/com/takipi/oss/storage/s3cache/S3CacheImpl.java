package com.takipi.oss.storage.s3cache;

import com.takipi.oss.storage.caching.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3CacheImpl implements S3Cache {

    private static final Logger logger = LoggerFactory.getLogger(S3CacheImpl.class);

    private final Cache cache;

    public S3CacheImpl(long maxSize, boolean enableCacheLogger) {
        InMemoryCache memoryCache = new InMemoryCache.Builder().setMaxSize(maxSize).build();
        CacheDelegator cacheDelegator = enableCacheLogger ? new CacheLogger(memoryCache) : memoryCache;
        cache = new Cache(cacheDelegator);
    }

    public String get(String key) {
        
        byte[] bytes = cache.get(key);
        
        if (bytes != null) {
            
            try {
                return new String(bytes, "UTF-8");
            }
            catch (Exception e) {
                logger.error("Failed to convert byte[] to String", e.getMessage());
            }
        }
        
        return null;
    }
    
    public void put(String key, String value) {
        cache.put(key, value.getBytes());
    }
}
