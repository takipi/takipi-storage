package com.takipi.oss.storage.data;

import com.takipi.oss.storage.fs.Record;

public class RecordWithData
{
    private Record record;
    private String data;
    
    private RecordWithData()
    {
        
    }
    
    public static RecordWithData of(Record record, String data) {
        RecordWithData recordWithData = new RecordWithData();
        recordWithData.record = record;
        recordWithData.data = data;
        
        return recordWithData;
    }
    
    public Record getRecord() {
        return record;
    }
    
    public void setRecord(Record record) {
        this.record = record;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
}
