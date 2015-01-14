package com.takipi.oss.storage.fs;

public class Record {
    private final String serviceId;
    private final String type;
    private final String key;

    public Record(String serviceId, String type, String key) {
        this.serviceId = serviceId;
        this.type = type;
        this.key = key;
    }

    public static Record newRecord(String serviceId, String type, String key) {
        return new Record(serviceId, type, key);
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "service: " + serviceId + ". type: " + type + ". key: " + key + ".";
    }
}
