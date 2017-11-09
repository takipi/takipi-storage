package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.concurrent.SimpleStopWatch;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3ObjectFetcherTask implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(S3ObjectFetcherTask.class);
    
    private final RecordWithData recordWithData;
    private final Filesystem<Record> filesystem;
    private final EncodingType encodingType;
    
    S3ObjectFetcherTask(RecordWithData recordWithData, Filesystem<Record> filesystem, EncodingType encodingType) {
        this.recordWithData = recordWithData;
        this.filesystem = filesystem;
        this.encodingType = encodingType;
    }
    
    @Override
    public void run() {
        String result = load(filesystem, recordWithData.getRecord(), encodingType);
        recordWithData.setData(result);
    }
    
    private static String load(Filesystem<Record> filesystem, Record record, EncodingType encodingType) {
        
        final SimpleStopWatch stopWatch = new SimpleStopWatch();
        final String key = record.getKey();
        String value = null;
        final int MAX_TRIES = 2;
        int count = 0;
        
        while ((value == null) && (count < MAX_TRIES)) {
            
            if (count++ > 0) {
                logger.warn("Retry loading object for key {}", key);
                stopWatch.reset();
            }
            
            try {
                value = FilesystemUtil.read(filesystem, record, encodingType);
            }
            catch (Exception e) {
                // Need this catch because some exceptions inside FilesystemUtil.read are caught and result in a
                // null return value, and some are thrown. The code would be simpler if all exceptions were thrown. 
            }
        }
    
        long elapsed = stopWatch.elapsed();
        
        if (value == null) {
            logger.error("Failed to load object for key: {}. Elapsed time = {} ms", key, elapsed);
            throw new RuntimeException("Failed to load object for key: " + key);
        }
    
        logger.debug("{} loaded key {} in {} ms. {} bytes", Thread.currentThread().getName(), key, elapsed, value.length());
        return value;
    }
}
