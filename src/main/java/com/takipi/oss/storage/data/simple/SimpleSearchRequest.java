package com.takipi.oss.storage.data.simple;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.fs.api.SearchRequest;

public class SimpleSearchRequest implements SearchRequest {
    public EncodingType encodingType;
    public String name;
    public String baseSearchPath;
    public boolean preventDuplicates;

    @Override
    public EncodingType getEncodingType() {
        return encodingType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBaseSearchPath() {
        return baseSearchPath;
    }

    @Override
    public boolean shouldPreventDuplicates() {
        return preventDuplicates;
    }
}
