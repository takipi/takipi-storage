package com.takipi.oss.storage.caching;

public class HashLock {
    private static final int DEFAULT_SIZE = 32;

    private final Object[] locks;

    public HashLock() {
        this(DEFAULT_SIZE);
    }

    public HashLock(int size) {
        locks = new Object[size];

        for (int i = 0; i < size; i++) {
            locks[i] = new Object();
        }
    }

    public Object get(Object key) {
        int hashCode = key.hashCode();

        if (hashCode < 0) {
            hashCode = (hashCode + 1) + Integer.MAX_VALUE;
        }

        int bucket = hashCode % locks.length;

        return locks[bucket];
    }
}
