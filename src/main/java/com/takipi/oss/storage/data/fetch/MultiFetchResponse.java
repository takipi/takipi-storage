package com.takipi.oss.storage.data.fetch;

import java.util.List;

import com.takipi.oss.storage.data.RecordWithData;

public class MultiFetchResponse {
    List<RecordWithData> records;
    
    public MultiFetchResponse(List<RecordWithData> records) {
        this.records = records;
    }
    
    public List<RecordWithData> getRecords() {
        return records;
    }
}
