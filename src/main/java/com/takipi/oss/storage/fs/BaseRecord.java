package com.takipi.oss.storage.fs;

public interface BaseRecord {
    public String getServiceId();

    public String getType();

    public String getKey();

    public String getPath();
}
