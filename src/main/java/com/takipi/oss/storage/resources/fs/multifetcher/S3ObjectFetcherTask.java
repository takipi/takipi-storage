package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.concurrent.SimpleStopWatch;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3ObjectFetcherTask implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(S3ObjectFetcherTask.class);
  
  private final RecordWithData recordWithData;
  
  private final Filesystem<BaseRecord> filesystem;
  
  private final EncodingType encodingType;
  
  S3ObjectFetcherTask(RecordWithData recordWithData, Filesystem<BaseRecord> filesystem, EncodingType encodingType) {
    this.recordWithData = recordWithData;
    this.filesystem = filesystem;
    this.encodingType = encodingType;
  }
  
  public void run() {
    String result = load(this.filesystem, this.recordWithData.getRecord(), this.encodingType);
    if (result != null)
      this.recordWithData.setData(result); 
  }
  
  private static String load(Filesystem<BaseRecord> filesystem, Record record, EncodingType encodingType) {
    SimpleStopWatch stopWatch = new SimpleStopWatch();
    String key = record.getKey();
    String value = null;
    int MAX_TRIES = 2;
    int count = 0;
    while (value == null && count < 2) {
      if (count++ > 0) {
        logger.warn("Retry loading object for key {}", key);
        stopWatch.reset();
      } 
      try {
        value = FilesystemUtil.read(filesystem, (BaseRecord)record, encodingType);
      } catch (Exception exception) {}
    } 
    long elapsed = stopWatch.elapsed();
    if (value == null) {
      logger.error("Failed to load object for key: {}. Elapsed time = {} ms", key, Long.valueOf(elapsed));
      return null;
    } 
    logger.debug("{} loaded key {} in {} ms. {} bytes", new Object[] { Thread.currentThread().getName(), key, Long.valueOf(elapsed), Integer.valueOf(value.length()) });
    return value;
  }
}

