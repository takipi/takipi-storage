package com.takipi.oss.storage.resources.fs;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.helper.FilesystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/storage/v1/json/multifetch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMultiFetchStorageResource {
    
    private static final int MAX_CACHE_SIZE = 4194304;  // 4 MB
    private final static int MAX_THREADS = 50;
    
    private static final Logger logger = LoggerFactory.getLogger(JsonMultiFetchStorageResource.class);
    private static final AtomicInteger threadCount = new AtomicInteger();
    private static final ExecutorService executorService;
    private static final Map<String, String> cache;
    
    private final Filesystem<Record> filesystem;
    
    static {
        
        cache = Collections.synchronizedMap(
                new LinkedHashMap<String, String>(1024,0.75f, true) {
                    
                    private int cacheSize = 0;
                    
                    @Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                        boolean remove = cacheSize > MAX_CACHE_SIZE;
                        if (remove) {
                            cacheSize -= eldest.getValue().length();
                        }
                        return remove;
                    }
                    
                    @Override public String put(String key, String value) {
                        cacheSize += value.length();
                        return super.put(key, value);
                    }
                });
        
        executorService = Executors.newFixedThreadPool(MAX_THREADS, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r)
            {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("s3_get_thread_" + threadCount.incrementAndGet());
                return t;
            }
        });
    }
    
    public JsonMultiFetchStorageResource(Filesystem<Record> filesystem) {
        this.filesystem = filesystem;
    }
    
    @POST
    @Timed
    public Response post(MultiFetchRequest request) {
        try {
            MultiFetchResponse response = handleResponse(request);
            
            return Response.ok(response).build();
        }
        catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }
    
    private MultiFetchResponse handleResponse(MultiFetchRequest request) throws InterruptedException {
        final EncodingType encodingType = request.encodingType;
        final List<Record> recordsToRetrieve = request.records;
        final int count = recordsToRetrieve.size();
        final List<Future<String>> futures = new ArrayList<>(count);
        final List<RecordWithData> recordsWithData = new ArrayList<>(count);
        
        logger.info("---------- Multi Fetch Request for " + count + " records");
        
        StopWatch stopWatch = new StopWatch();
        
        for (Record record : recordsToRetrieve) {
            String value = cache.get(record.getKey());
            recordsWithData.add(RecordWithData.of(record, value));
            if (value != null) {
                logger.info("S3 key " + record.getKey() + " found in cache. " + value.length() + " bytes");
            }
            else {
                Callable<String> callable = new S3Callable(filesystem, record, encodingType);
                futures.add(executorService.submit(callable));
            }
        }
        
        for (int i = 0, futureIndex = 0; i < count; ++i) {
            RecordWithData recordWithData = recordsWithData.get(i);
            if (recordWithData.getData() == null) {
                try {
                    String value = futures.get(futureIndex++).get(20, TimeUnit.SECONDS);
                    recordWithData.setData(value);
                }
                catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        
        logger.info("---------- Multi Fetch Request for " + count + " records completed in " + stopWatch.elapsed() + " ms");
        
        return new MultiFetchResponse(recordsWithData);
    }
    
    private static class S3Callable implements Callable<String> {
        final Filesystem<Record> filesystem;
        final Record record;
        final EncodingType encodingType;
        
        S3Callable(Filesystem<Record> filesystem, Record record, EncodingType encodingType) {
            this.filesystem = filesystem;
            this.record = record;
            this.encodingType = encodingType;
        }
        
        @Override
        public String call() throws Exception {
            StopWatch stopWatch = new StopWatch();
            String value = null;
            final int MAX_TRIES = 2;
            int count = 0;
            
            while ((value == null) && (count < MAX_TRIES)) {
                
                if (count++ > 0) {
                    logger.warn("Retrying loading S3 key " + record.getKey() + ". Elapsed time = " + stopWatch.elapsed() + " ms");
                    stopWatch.reset();
                }
                
                try {
                    value = FilesystemUtil.read(filesystem, record, encodingType);
                }
                catch (Exception e) {
                    // Need this catch because some exceptions inside FilesystemUtil.read are caught and result in a
                    // null return value. The code would be simpler if all exceptions were just passed on. 
                }
            }
            
            if (value != null) {
                cache.put(record.getKey(), value);
                logger.info("--------------------- " + Thread.currentThread().getName() + " loaded S3 key " +
                        record.getKey() + " in " + stopWatch.elapsed() + " ms. " + value.length() + " bytes");
            }
            else {
                logger.error("Failed to load S3 key: " + record.getKey() + ". Elapsed time = " + stopWatch.elapsed() + " ms");
                throw new RuntimeException("Failed to load S3 key: " + record.getKey());
            }
            
            return value;
        }
    }
}
