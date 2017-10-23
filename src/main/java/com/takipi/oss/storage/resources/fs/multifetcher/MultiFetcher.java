package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.concurrent.SimpleStopWatch;
import com.takipi.oss.storage.fs.concurrent.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MultiFetcher {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiFetcher.class);
    private static long maxCacheSize = 33554432;
    
    private final TaskExecutor taskExecutor;
    private final static S3Cache cache;
    
    static {
        if (maxCacheSize > 0) {
            cache = new S3CacheImpl(maxCacheSize);
        }
        else {
            cache = new S3Cache() {
                @Override
                public String get(String key) {
                    return null;
                }
    
                @Override
                public void put(String key, String value) {
                }
            };
        }
    }
    
    public MultiFetcher(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    public static void setMaxCacheSize(long maxCacheSize) {
        MultiFetcher.maxCacheSize = maxCacheSize;
    }
    
    public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem) {
    
        SimpleStopWatch stopWatch = new SimpleStopWatch();
        List<Record> records = request.records;
        final int count = records.size();
        final List<RecordWithData> recordsWithData = new ArrayList<>(count);
        final List<RecordWithData> recordsToFetch = new ArrayList<>(count);
    
        logger.debug("------------ Multi fetcher commencing load of {} objects", count);
        
        for (Record record : records) {
            String value = cache.get(record.getKey());
            RecordWithData recordWithData = RecordWithData.of(record, value);
            recordsWithData.add(recordWithData);
            if (value == null) {
                recordsToFetch.add(recordWithData);
            }
            else {
                logger.debug("Multi fetcher found key {} in cache", record.getKey());
            }
        }
        
        final List<Runnable> tasks = new ArrayList<>(recordsToFetch.size());
        
        for (RecordWithData recordWithData : recordsToFetch) {
            tasks.add(new S3ObjectFetcherTask(recordWithData, filesystem, request.encodingType));
        }
        
        taskExecutor.execute(tasks);
        
        logger.debug("------------ Multi fetcher completed loading {} objects in {} ms", count, stopWatch.elapsed());
    
        for (RecordWithData recordWithData : recordsToFetch) {
            cache.put(recordWithData.getRecord().getKey(), recordWithData.getData());
        }
    
        return new MultiFetchResponse(recordsWithData);
    }
}
