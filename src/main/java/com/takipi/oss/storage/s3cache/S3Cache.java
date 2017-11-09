package com.takipi.oss.storage.s3cache;

public interface S3Cache {

    String get(String key);
    
    void put(String key, String value);
}
