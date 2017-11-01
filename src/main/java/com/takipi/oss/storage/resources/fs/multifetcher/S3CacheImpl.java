package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.caching.Cache;
import com.takipi.oss.storage.caching.InMemoryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3CacheImpl implements S3Cache {

    private static final Logger logger = LoggerFactory.getLogger(S3CacheImpl.class);

    private final InMemoryCache memoryCache;
    private final Cache cache;

    S3CacheImpl(long maxSize) {
        memoryCache = new InMemoryCache.Builder().setMaxSize(maxSize).build();
        cache = new Cache(memoryCache);
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
