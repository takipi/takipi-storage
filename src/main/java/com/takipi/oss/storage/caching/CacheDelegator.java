package com.takipi.oss.storage.caching;

public abstract class CacheDelegator {
    private final CacheDelegator parent;

    public CacheDelegator() {
        this(null);
    }

    public CacheDelegator(CacheDelegator parent) {
        this.parent = parent;
    }

    public <V> SerializableCacheValue<V> parentGet(String key, SerializableCacheValue<V> result) {
        if (this.parent != null) {
            return this.parent.get(key, result);
        }

        return result;
    }

    public <V> SerializableCacheValue<V> parentPut(String key, SerializableCacheValue<V> value) {
        return parentPut(key, value, false);
    }

    public <V> SerializableCacheValue<V> parentPut(String key, SerializableCacheValue<V> value, boolean overwrite) {
        if (this.parent != null) {
            return this.parent.put(key, value, overwrite);
        }

        return value;
    }

    @Override
    public String toString() {
        if (this.parent == null) {
            return "NUL";
        }

        return this.parent.toString();
    }

    public <V> SerializableCacheValue<V> put(String key, SerializableCacheValue<V> value) {
        return put(key, value, false);
    }

    public abstract <V> SerializableCacheValue<V> put(String key, SerializableCacheValue<V> value, boolean overwrite);

    public abstract <V> SerializableCacheValue<V> get(String key, SerializableCacheValue<V> result);
}
