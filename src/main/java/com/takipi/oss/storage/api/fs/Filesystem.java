package com.takipi.oss.storage.api.fs;

import java.io.IOException;

public interface Filesystem {
    void putBytes(String folder, String key, byte[] bytes) throws IOException;
    byte[] getBytes(String folder, String key) throws IOException;
    
    void putJson(String folder, String key, String string) throws IOException;
    String getJson(String folder, String key) throws IOException;
}
