package com.takipi.oss.storage.data.simple;

public class SimpleSearchResponse {
    String data;
    String path;
    
    public SimpleSearchResponse(String data, String path) {
        this.data = data;
        this.path = path;
    }
    
    public String getData() {
        return data;
    }
    
    public String getPath() {
        return path;
    }
}
