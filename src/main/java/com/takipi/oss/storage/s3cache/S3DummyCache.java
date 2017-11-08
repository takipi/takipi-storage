package com.takipi.oss.storage.s3cache;

public class S3DummyCache implements S3Cache {

    public static final S3DummyCache instance = new S3DummyCache();
    
    private S3DummyCache() {
        
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void put(String key, String value) {

    }
}
