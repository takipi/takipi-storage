package com.takipi.oss.storage.fs;

import org.apache.commons.lang.StringUtils;

public class Record {
    private String serviceId;
    private String type;
    private String key;

    public Record() {

    }

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

    @Override
    public int hashCode() {
        return (key != null) ? key.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Record)) {
            return false;
        }

        Record objRecord = (Record) obj;

        return ((StringUtils.equals(serviceId, objRecord.serviceId)) &&
                (StringUtils.equals(type, objRecord.type)) &&
                (StringUtils.equals(key, objRecord.key)));
    }
}
