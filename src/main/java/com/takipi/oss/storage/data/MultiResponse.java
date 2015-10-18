package com.takipi.oss.storage.data;

import java.util.List;

public class MultiResponse {
    List<RecordWithData> records;
    
    public MultiResponse(List<RecordWithData> records) {
        this.records = records;
    }
    
    public List<RecordWithData> getRecords() {
        return records;
    }
}
