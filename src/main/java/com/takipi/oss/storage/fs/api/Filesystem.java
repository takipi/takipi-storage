package com.takipi.oss.storage.fs.api;

import java.io.IOException;
import java.io.InputStream;

public interface Filesystem<T> extends FilesystemHealth {
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
    void put(T record, InputStream is) throws IOException;

    /**
     * Get record
     * 
     * @param record
     *            - the record to get
     * @return the input stream of the record
     * @throws IOException
     *             - if there's an error
     */
    InputStream get(T record) throws IOException;

    /**
     * Removes record from filesystem
     * 
     * @param record
     *            - the name of the record to remove
     * @throws IOException
     *             - if there's an error
     */
    void delete(T record) throws IOException;

    /**
     * True if record exists in filesystem
     * 
     * @param record
     *            - the name of the record to remove
     * @throws IOException
     *             - if there's an error
     */
    boolean exists(T record) throws IOException;

     /**
     * Returns the size of a record in the filesystem
     * 
     * @param record
     *            - the name of the record to get its size
     * @throws IOException
     *             - if there's an error
     */
    long size(T record) throws IOException;
}
