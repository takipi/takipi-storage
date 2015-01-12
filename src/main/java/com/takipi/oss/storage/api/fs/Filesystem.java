package com.takipi.oss.storage.api.fs;

import java.io.IOException;

public interface Filesystem {
    /**
     * Put bytes (binary)
     * 
     * @param key
     *            - the key acts as the name
     * @param bytes
     *            - the byte array to save
     * @throws IOException
     *             - if there's an error
     */
    void putBytes(String key, byte[] bytes) throws IOException;

    /**
     * Get bytes
     * 
     * @param key
     *            - the key acts as the name
     * @return the byte array
     * @throws IOException
     *             - if there's an error
     */
    byte[] getBytes(String key) throws IOException;

    /**
     * Put String (json)
     * 
     * @param key
     *            - the key acts as the name
     * @param bytes
     *            - the string to save
     * @throws IOException
     *             - if there's an error
     */
    void putJson(String key, String string) throws IOException;

    /**
     * Get string
     * 
     * @param key
     *            - the key acts as the name
     * @return the string
     * @throws IOException
     *             - if there's an error
     */
    String getJson(String key) throws IOException;
}
