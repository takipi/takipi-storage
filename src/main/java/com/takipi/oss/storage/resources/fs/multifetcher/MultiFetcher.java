package com.takipi.oss.storage.resources.fs.multifetcher;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.data.RecordWithData;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.concurrent.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MultiFetcher {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiFetcher.class);
    
    private final TaskExecutor taskExecutor;
    
    public MultiFetcher(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    public MultiFetchResponse loadData(MultiFetchRequest request, Filesystem<Record> filesystem) {
        
        final int count = request.records.size();
        final EncodingType encodingType = request.encodingType;
        final List<Runnable> tasks = new ArrayList<>(count);
        
        List<RecordWithData> recordsWithData = new ArrayList<>(count);
        
        for (Record record : request.records) {
            RecordWithData recordWithData = RecordWithData.of(record, null);
            recordsWithData.add(recordWithData);
            tasks.add(new S3ObjectFetcherTask(recordWithData, filesystem, encodingType));
        }
        
        taskExecutor.execute(tasks);
        
        logger.debug("Multi fetched completed fetching of {} objects", count);
        
        return new MultiFetchResponse(recordsWithData);
    }
}
