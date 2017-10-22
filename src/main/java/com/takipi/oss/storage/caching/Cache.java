package com.takipi.oss.storage.caching;

public class Cache {
    private final CacheDelegator cacheDelegator;

    public Cache(CacheDelegator cacheDelegator) {
        this.cacheDelegator = cacheDelegator;
    }

    public <V> void put(String key, V value, Serializer<V> serializer) {
        if (!canCache()) {
            return;
        }

        cacheDelegator.put(key, new SerializableCacheValue<V>(value, serializer));
    }

    public void put(String key, byte[] value) {
        if (!canCache()) {
            return;
        }

        cacheDelegator.put(key, new SerializableCacheValue<byte[]>(value, ByteArraySerializer.instance));
    }

    public <V> void put(String key, V value, byte[] serializedValue) {
        if (!canCache()) {
            return;
        }

        cacheDelegator.put(key, new SerializableCacheValue<V>(value, serializedValue));
    }

    public byte[] get(String key) {
        if (!canCache()) {
            return null;
        }

        SerializableCacheValue<byte[]> result =
                cacheDelegator.get(key, new SerializableCacheValue<byte[]>(byte[].class, ByteArraySerializer.instance));

        return result.deserialize(key);
    }

    public <V> V get(String key, Class<V> valueClass, Serializer<V> serializer) {
        if (!canCache()) {
            return null;
        }

        SerializableCacheValue<V> result =
                cacheDelegator.get(key, new SerializableCacheValue<V>(valueClass, serializer));

        return result.deserialize(key);
    }

    protected boolean canCache() {
        return true;
    }

    public static interface Serializer<V> {
        public byte[] serialize(V value) throws Exception;

        public V deserialize(byte[] bytes) throws Exception;
    }

    public static class ByteArraySerializer implements Serializer<byte[]> {
        public static ByteArraySerializer instance = new ByteArraySerializer();

        @Override
        public byte[] serialize(byte[] value) throws Exception {
            return value;
        }

        @Override
        public byte[] deserialize(byte[] bytes) throws Exception {
            return bytes;
        }
    }
}
