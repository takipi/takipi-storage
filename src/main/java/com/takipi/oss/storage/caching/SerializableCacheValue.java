package com.takipi.oss.storage.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializableCacheValue<V> {
    private static final Logger logger = LoggerFactory.getLogger(SerializableCacheValue.class);

    private V value;
    private Cache.Serializer<V> serializer;
    private byte[] serializedValue;
    private Class<V> valueClass;

    private CacheDelegator firstRetriever;
    private CacheDelegator lastUpdater;

    public SerializableCacheValue(V value, Cache.Serializer<V> serializer) {
        this.value = value;
        this.serializer = serializer;
    }

    public SerializableCacheValue(Class<V> valueClass, Cache.Serializer<V> serializer) {
        this.valueClass = valueClass;
        this.serializer = serializer;
    }

    public SerializableCacheValue(V value, byte[] serializedValue) {
        this.value = value;
        this.serializedValue = serializedValue;
    }

    public CacheDelegator getRetriever() {
        return this.firstRetriever;
    }

    public CacheDelegator getUpdater() {
        return this.lastUpdater;
    }

    public void setUpdater(CacheDelegator updater) {
        updateUpdater(updater);
    }

    public void setSerializedValue(CacheDelegator retriever, byte[] serializedValue) {
        updateRetriever(retriever);
        this.serializedValue = serializedValue;
    }

    public void setValue(CacheDelegator retriever, Object value) {
        if (!valueClass.isInstance(value)) {
            logger.error("Error setting value, Type mismatch (expected: {}; found: {}).",
                    valueClass.getSimpleName(), value.getClass().getSimpleName());
            return;
        }

        updateRetriever(retriever);
        this.value = valueClass.cast(value);
    }

    private void updateRetriever(CacheDelegator retriever) {
        if (this.firstRetriever == null) {
            this.firstRetriever = retriever;
        }
    }

    private void updateUpdater(CacheDelegator updater) {
        this.lastUpdater = updater;
    }

    public V deserialize(String name) {
        try {
            if (value == null) {
                if (serializedValue == null) {
                    return null;
                }

                value = serializer.deserialize(serializedValue);

                if (!valueClass.isInstance(value)) {
                    logger.error("Error serializing, Type mismatch for '{}' in cache (expected: {}; found: {}).",
                            name, valueClass.getSimpleName(), value.getClass().getSimpleName());

                    return null;
                }

                return valueClass.cast(value);
            }

            return value;
        }
        catch (Exception e) {
            logger.error("Error deserializing '{}'", name, e);
            return null;
        }
    }

    public byte[] serialize(String name) {
        try {
            if (serializedValue == null) {
                serializedValue = serializer.serialize(value);
            }

            return serializedValue;
        }
        catch (Exception e) {
            logger.error("Error serializing '{}'", name, e);
            return null;
        }
    }
}
