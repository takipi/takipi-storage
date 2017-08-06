package com.takipi.oss.storage.fs.api;

import java.io.IOException;
import java.io.InputStream;

import com.takipi.oss.storage.fs.BaseRecord;

public interface Filesystem<T extends BaseRecord> extends FilesystemHealth {
    /**
     * Put record
     * 
     * @param record
     *            - the record to save the input stream to
     * @param is
     *            - the input stream to save
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


    /**
     * Returns the {@link SearchResult} that matches the search query.
     *
     * @return
     * @throws IOException
     * @param request
     */

    /**
     * Returns the {@link SearchResult} that matches the search query.
     *
     * @param searchRequest
     *          - the search request
     * @return
     *          - the result of the search or null if nothing was found.
     * @throws IOException
     *          - if there's an error
     */
    SearchResult search(SearchRequest searchRequest) throws IOException;

    /**
     * Convert string path to record object
	 * 
     * @param path
     * @return record
     */
    BaseRecord pathToRecord(String path);

}
