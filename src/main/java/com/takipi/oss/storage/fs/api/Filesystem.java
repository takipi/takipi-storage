package com.takipi.oss.storage.fs.api;

import java.io.IOException;
import java.io.InputStream;

import com.takipi.oss.storage.fs.Record;

public interface Filesystem {
    /**
     * Put record
     * 
     * @param record
     *            - the record to save the input stream to
     * @param bytes
     *            - the byte array to save
     * @throws IOException
     *             - if there's an error
     */
    void put(Record record, InputStream is) throws IOException;

    /**
     * Get record
     * 
     * @param record
     *            - the record to get
     * @return the input stream of the record
     * @throws IOException
     *             - if there's an error
     */
    InputStream get(Record record) throws IOException;

    /**
     * Removes record from filesystem
     * 
     * @param record
     *            - the name of the record to remove
     * @throws IOException
     *             - if there's an error
     */
    void delete(Record record) throws IOException;

    /**
     * True if record exists in filesystem
     * 
     * @param record
     *            - the name of the record to remove
     * @throws IOException
     *             - if there's an error
     */
    boolean exists(Record record) throws IOException;

    /**
     * Checks if the filesystem is healthy. This will be called during health
     * check
     * 
     * @return true iff the filesystem is healthy
     */
    boolean healthy();
}
