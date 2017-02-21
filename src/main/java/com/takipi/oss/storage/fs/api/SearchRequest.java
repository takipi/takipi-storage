package com.takipi.oss.storage.fs.api;

import com.takipi.oss.storage.data.EncodingType;

public interface SearchRequest {

    EncodingType getEncodingType();

    String getName();

    String getBaseSearchPath();

    boolean shouldPreventDuplicates();

}
