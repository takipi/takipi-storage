package com.takipi.oss.storage.api.fs;

import java.io.IOException;

public interface Filesystem {
    void put(String folder, String key, byte[] bytes) throws IOException;
    byte[] get(String folder, String key) throws IOException;
}
