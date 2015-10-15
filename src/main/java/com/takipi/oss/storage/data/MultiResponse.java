package com.takipi.oss.storage.data;

import java.util.Map;

import com.google.common.collect.Maps;

public class MultiResponse {
    public Map<String, String> records;

    public MultiResponse() {
        records = Maps.newHashMap();
    }
}
