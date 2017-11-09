package com.takipi.oss.storage.caching;

import java.util.concurrent.atomic.AtomicLong;

import com.takipi.oss.storage.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheLogger extends CacheDelegator {
    private static final Logger logger = LoggerFactory.getLogger(CacheLogger.class);

    private static final AtomicLong counter = new AtomicLong();

    private final long id;

    public CacheLogger(CacheDelegator parent) {
        super(parent);

        this.id = counter.incrementAndGet();

        logger.info("Cache created (id: {}) {}", this.id, parent);
    }

    @Override
    public <V> SerializableCacheValue<V> get(String key, SerializableCacheValue<V> result) {
        long start = System.currentTimeMillis();

        try {
            return parentGet(key, result);
        }
        finally {
            CacheDelegator delegator = result.getRetriever();

            if (delegator != null) {
                logger.info("{} {}ms {} (retriever: {}) {}",
                        paddedId(),
                        paddedDiff(start),
                        paddedVerb("get"),
                        paddedDelegator(delegator),
                        key);
            }
        }
    }

    @Override
    public <V> SerializableCacheValue<V> put(String key, SerializableCacheValue<V> value) {
        return parentPut(key, value, false);
    }

    @Override
    public <V> SerializableCacheValue<V> put(String key, SerializableCacheValue<V> value, boolean overwrite) {
        long start = System.currentTimeMillis();

        try {
            return parentPut(key, value, overwrite);
        }
        finally {
            CacheDelegator delegator = value.getUpdater();

            if (delegator != null) {
                logger.info("{} {}ms {} (updater:   {}) {}",
                        paddedId(),
                        paddedDiff(start),
                        paddedVerb("put"),
                        paddedDelegator(delegator),
                        key);
            }
        }
    }

    private String paddedId() {
        return StringUtil.padRight(Long.toString(id), 8);
    }

    private String paddedVerb(String verb) {
        return StringUtil.padRight(verb, 10);
    }

    private String paddedDiff(long start) {
        long diff = System.currentTimeMillis() - start;

        return StringUtil.padLeft(Long.toString(diff), 8);
    }

    private String paddedDelegator(CacheDelegator delegator) {
        return StringUtil.padLeft(delegator.getClass().getSimpleName(), 15);
    }
}
