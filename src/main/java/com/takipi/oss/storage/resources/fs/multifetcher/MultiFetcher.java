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

    private static final long DEFAULT_MAX_CACHE_SIZE = 33554432; // 32 MB

    private static class PartSizeEstimator {

        private static long MIN_LOADED_PARTS_FOR_SIZE_ESTIMATION = 10;
        private static int DEFAULT_PART_SIZE_ESTIMATION = 1700;
        private static long MAX_TOTAL_SIZE = 1L << 30;

        private long totalSizeLoaded = 0;
        private long numberOfPartsLoaded = 0;

        synchronized void updateStats(long size) {
            if (totalSizeLoaded < MAX_TOTAL_SIZE) {
                totalSizeLoaded += size;
                ++numberOfPartsLoaded;
            }
        }

        synchronized int getEstimatedSizePerPart() {
            if (numberOfPartsLoaded < MIN_LOADED_PARTS_FOR_SIZE_ESTIMATION) {
                return DEFAULT_PART_SIZE_ESTIMATION;
            }
            else {
                return (int)(totalSizeLoaded / numberOfPartsLoaded);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MultiFetcher.class);
    private static long maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    
    private final TaskExecutor taskExecutor;
    private final static S3Cache cache;
    private static final PartSizeEstimator partSizeEstimator = new PartSizeEstimator();
    
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

        int estimatedSizePerPart = partSizeEstimator.getEstimatedSizePerPart();
        final int maxBatchCount = request.maxBatchSize / estimatedSizePerPart;
        logger.info("Max batch size = {}. Estimated size per part = {}. Max batch count = {}",
                request.maxBatchSize, estimatedSizePerPart, maxBatchCount);

        List<Record> records = request.records;
        records = (records.size() > maxBatchCount) ? records.subList(0, maxBatchCount) : records;

        final SimpleStopWatch stopWatch = new SimpleStopWatch();
        final int count = records.size();
        final List<RecordWithData> recordsWithData = new ArrayList<>(count);
        final List<RecordWithData> recordsToFetch = new ArrayList<>(count);

        logger.debug("------------ Multi fetcher commencing load of {} objects", count);

        long totalSize = 0;

        for (Record record : records) {
            String value = cache.get(record.getKey());
            RecordWithData recordWithData = RecordWithData.of(record, value);
            recordsWithData.add(recordWithData);
            if (value == null) {
                recordsToFetch.add(recordWithData);
            }
            else {
                totalSize += value.length();
                logger.debug("Multi fetcher found key {} in cache. {} bytes", record.getKey(), value.length());
            }
        }
        
        if (!recordsToFetch.isEmpty()) {

            final List<Runnable> tasks = new ArrayList<>(recordsToFetch.size());

            for (RecordWithData recordWithData : recordsToFetch) {
                tasks.add(new S3ObjectFetcherTask(recordWithData, filesystem, request.encodingType));
            }

            taskExecutor.execute(tasks);

            for (RecordWithData recordWithData : recordsToFetch) {
                String value = recordWithData.getData();
                cache.put(recordWithData.getRecord().getKey(), value);
                totalSize += value.length();
                partSizeEstimator.updateStats(value.length());
            }
        }

        logger.info("------------ Multi fetcher completed loading {} objects in {} ms. Total bytes fetched = {}",
                count, stopWatch.elapsed(), totalSize);

        return new MultiFetchResponse(recordsWithData);
    }
}
