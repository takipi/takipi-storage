package com.takipi.oss.storage.metric;

public interface StorageMetric {
    void getStarted();

    void getDone(long size);

    void putStarted();

    void putDone(long size);

    void deleteStarted();

    void deleteDone();

    void headStarted();

    void headDone();
}
