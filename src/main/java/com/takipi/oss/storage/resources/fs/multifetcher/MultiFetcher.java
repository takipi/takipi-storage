package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.concurrent.SimpleStopWatch;
import com.takipi.oss.storage.fs.concurrent.TaskExecutor;
import com.takipi.oss.storage.s3cache.S3Cache;
import com.takipi.oss.storage.s3cache.S3DummyCache;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiFetcher {
  private static class PartSizeEstimator {
    private static long MIN_LOADED_PARTS_FOR_SIZE_ESTIMATION = 10L;
    
    private static int DEFAULT_PART_SIZE_ESTIMATION = 1700;
    
    private long totalSizeLoaded = 0L;
    
    private long numberOfPartsLoaded = 0L;
    
    synchronized void updateStats(long size) {
      this.totalSizeLoaded += size;
      this.numberOfPartsLoaded++;
    }
    
    synchronized int getEstimatedSizePerPart() {
      if (this.numberOfPartsLoaded < MIN_LOADED_PARTS_FOR_SIZE_ESTIMATION)
        return DEFAULT_PART_SIZE_ESTIMATION; 
      return (int)(this.totalSizeLoaded / this.numberOfPartsLoaded);
    }
    
    private PartSizeEstimator() {}
  }
  
  private static final Logger logger = LoggerFactory.getLogger(MultiFetcher.class);
  
  private final TaskExecutor taskExecutor;
  
  private final S3Cache cache;
  
  private final PartSizeEstimator partSizeEstimator = new PartSizeEstimator();
  
  private final int maxBatchSize;
  
  public MultiFetcher(TaskExecutor taskExecutor, int maxCacheSize, boolean enableCacheLogger, int maxBatchSize) {
    this.taskExecutor = taskExecutor;
    this.cache = (S3Cache)S3DummyCache.instance;
    this.maxBatchSize = maxBatchSize;
  }
  
  public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<BaseRecord> filesystem) {
    int estimatedSizePerPart = this.partSizeEstimator.getEstimatedSizePerPart();
    int maxBatchCount = this.maxBatchSize / estimatedSizePerPart;
    logger.info("Max batch size = {}. Estimated size per part = {}. Max batch count = {}", new Object[] { Integer.valueOf(this.maxBatchSize), Integer.valueOf(estimatedSizePerPart), Integer.valueOf(maxBatchCount) });
    List<Record> records = request.records;
    records = (records.size() > maxBatchCount) ? records.subList(0, maxBatchCount) : records;
    SimpleStopWatch stopWatch = new SimpleStopWatch();
    int count = records.size();
    List<RecordWithData> recordsWithData = new ArrayList<>(count);
    List<RecordWithData> recordsToFetch = new ArrayList<>(count);
    logger.debug("Multi fetcher commencing load of {} objects", Integer.valueOf(count));
    long totalSize = 0L;
    for (BaseRecord record : records) {
      String value = this.cache.get(record.getKey());
      RecordWithData recordWithData = RecordWithData.of((Record)record, value);
      recordsWithData.add(recordWithData);
      if (value == null) {
        recordsToFetch.add(recordWithData);
        continue;
      } 
      totalSize += value.length();
      logger.debug("Multi fetcher found key {} in cache. {} bytes", record.getKey(), Integer.valueOf(value.length()));
    } 
    if (!recordsToFetch.isEmpty()) {
      List<Runnable> tasks = new ArrayList<>(recordsToFetch.size());
      for (RecordWithData recordWithData : recordsToFetch)
        tasks.add(new S3ObjectFetcherTask(recordWithData, filesystem, request.encodingType)); 
      this.taskExecutor.execute(tasks);
      for (RecordWithData recordWithData : recordsToFetch) {
        String value = recordWithData.getData();
        if (value != null) {
          this.cache.put(recordWithData.getRecord().getKey(), value);
          totalSize += value.length();
          this.partSizeEstimator.updateStats(value.length());
        } 
      } 
    } 
    logger.info("Multi fetcher loaded {} parts in {} ms. {} parts found in cache. {} bytes total.", new Object[] { Integer.valueOf(count), Long.valueOf(stopWatch.elapsed()), Integer.valueOf(records.size() - recordsToFetch.size()), Long.valueOf(totalSize) });
    return new MultiFetchResponse(recordsWithData);
  }
}

