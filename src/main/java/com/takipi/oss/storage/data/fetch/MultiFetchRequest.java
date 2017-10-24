package com.takipi.oss.storage.data.fetch;

import java.util.List;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.fs.Record;

public class MultiFetchRequest {
    public EncodingType encodingType;
    public List<Record> records;
    public int maxBatchSize;
}
