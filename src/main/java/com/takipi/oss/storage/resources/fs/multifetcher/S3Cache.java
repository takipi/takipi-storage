package com.takipi.oss.storage.resources.fs.multifetcher;

interface S3Cache {

    String get(String key);
    
    void put(String key, String value);
}
