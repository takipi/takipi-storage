package com.takipi.oss.storage.data.delete;

import java.util.List;

import com.takipi.oss.storage.fs.Record;

public class MultiDeleteResponse {
    List<Record> records;
    
    public MultiDeleteResponse(List<Record> records) {
        this.records = records;
    }
    
    public List<Record> getRecords() {
        return records;
    }
}
