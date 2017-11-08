package com.takipi.oss.storage.caching;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Weigher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class InMemoryCache extends CacheDelegator {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

    private final Cache<String, Object> cache;
    private final String description;

    public static class Builder {
        private CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        private CacheDelegator parent = null;
        private String description = "Memory Cache";

        public Builder setParentDelegator(CacheDelegator parent) {
            this.parent = parent;
            return this;
        }

        public Builder setMaxElementCount(int maxElementCount) {
            cacheBuilder.maximumSize(maxElementCount);
            description += " (max elements = " + maxElementCount + ")";
            return this;
        }

        public Builder setMaxSize(long maxSize) {
            Weigher<String, Object> weigher = new Weigher<String, Object>() {
                @Override
                public int weigh(String s, Object o) {
                    byte[] bytes = (byte[]) o;
                    return s.length() + bytes.length;
                }
            };
            cacheBuilder.weigher(weigher);
            cacheBuilder.maximumWeight(maxSize);
            description += " (max size = " + maxSize + ")";
            return this;
        }

        public Builder setExpiry(int expiry, TimeUnit timeUnit) {
            cacheBuilder.expireAfterAccess(expiry, timeUnit);
            description += " (expiry = " + expiry + " " + timeUnit + ")";
            return this;
        }

        public InMemoryCache build() {
            Cache<String, Object> cache = cacheBuilder.build();
            return new InMemoryCache(parent, cache, description);
        }
    }

    private InMemoryCache(CacheDelegator parent, Cache<String, Object> cache, String description) {
        super(parent);

        this.cache = cache;
        this.description = description;
    }

    @Override
    public String toString() {
        return description + " -> " + super.toString();
    }

    @Override
    public <V> SerializableCacheValue<V> get(String key, SerializableCacheValue<V> result) {
        try {
            return internalGet(key, result);
        }
        catch (Exception e) {
            logger.error("Error getting from in memory cache {}", key, e);
            return result;
        }
    }

    public <V> SerializableCacheValue<V> internalGet(final String key, final SerializableCacheValue<V> result) {
        Object value = null;

        try {
            value = cache.get(key, new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    SerializableCacheValue<V> parentResult = InMemoryCache.this.parentGet(key, result);

                    Object resultObject = null;

                    if (parentResult != null) {
                        resultObject = parentResult.deserialize(key);
                    }

                    if (resultObject == null) {
                        throw new IllegalStateException("Object with key: " + key + "not found in cache");
                    }

                    return resultObject;
                }
            });
        }
        catch (Exception e) {
        }

        if (value != null) {
            result.setValue(this, value);
        }

        return result;
    }

    @Override
    public <V> SerializableCacheValue<V> put(String key, SerializableCacheValue<V> value, boolean overwrite) {
        try {
            // The google cache we are using automatically overwrites values if a key exists
            // so we don't need to pass the overwrite param
            return internalPut(key, value);
        }
        catch (Exception e) {
            logger.error("Error putting to in memory cache {}", key, e);

            return value;
        }
    }

    public <V> SerializableCacheValue<V> internalPut(final String key, final SerializableCacheValue<V> value) {
        if (cache.getIfPresent(key) != null) {
            return value;
        }

        value.setUpdater(InMemoryCache.this);
        cache.put(key, value.deserialize(key));

        return InMemoryCache.this.parentPut(key, value);
    }

    public long size() {
        return cache.size();
    }
}
