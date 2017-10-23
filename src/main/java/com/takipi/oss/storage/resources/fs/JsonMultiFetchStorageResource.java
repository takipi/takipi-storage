package com.takipi.oss.storage.resources.fs;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.s3.S3Filesystem;
import com.takipi.oss.storage.resources.fs.multifetcher.MultiFetcher;
import com.takipi.oss.storage.fs.concurrent.TaskExecutor;
import com.takipi.oss.storage.fs.concurrent.ConcurrentTaskExecutor;
import com.takipi.oss.storage.fs.concurrent.SequentialTaskExecutor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/storage/v1/json/multifetch")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonMultiFetchStorageResource {

    private final Filesystem<Record> filesystem;
    private final MultiFetcher multiFetcher;

    public JsonMultiFetchStorageResource(Filesystem<Record> filesystem, int multiFetcherConcurrencyLevel) {

        this.filesystem = filesystem;
        
        TaskExecutor taskExecutor;

        if ((filesystem instanceof S3Filesystem) && (multiFetcherConcurrencyLevel > 1)) {
            taskExecutor = new ConcurrentTaskExecutor(multiFetcherConcurrencyLevel);
        }
        else {
            taskExecutor = new SequentialTaskExecutor();
        }

        this.multiFetcher = new MultiFetcher(taskExecutor);
    }

    @POST
    @Timed
    public Response post(MultiFetchRequest request) {

        try {
            MultiFetchResponse response = multiFetcher.loadData(request, filesystem);
            return Response.ok(response).build();
        }
        catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }

}
