package com.takipi.oss.storage.fs.api;

import java.io.IOException;

import com.takipi.oss.storage.fs.Record;

public interface Filesystem {
    /**
     * Put bytes (binary)
     * 
     * @param record
     *            - the record to put bytes to
     * @param bytes
     *            - the byte array to save
     * @throws IOException
     *             - if there's an error
     */
    void putBytes(Record record, byte[] bytes) throws IOException;

    /**
     * Get bytes
     * 
     * @param record
     *            - the record to get
     * @return the byte array
     * @throws IOException
     *             - if there's an error
     */
    byte[] getBytes(Record record) throws IOException;

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

    /**
     * Removes key from filesystem
     * 
     * @param record
     *            - the name of the record to remove
     * @throws IOException
     *             - if there's an error
     */
    void delete(Record record) throws IOException;

    /**
     * Checks if the filesystem is healthy. This will be called during health
     * check
     * 
     * @return true iff the filesystem is healthy
     */
    boolean healthy();
}
