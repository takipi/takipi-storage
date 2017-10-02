package com.takipi.oss.storage.resources.fs;

import com.codahale.metrics.annotation.Timed;
import com.takipi.oss.storage.data.fetch.MultiFetchRequest;
import com.takipi.oss.storage.data.fetch.MultiFetchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.cache.Cache;
import com.takipi.oss.storage.fs.cache.InMemoryCache;
import com.takipi.oss.storage.resources.fs.multifetcher.*;

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
    
    private static final int MAX_CACHE_SIZE = 8388608;  // 8 MB
    
    private static final Cache cache = new InMemoryCache(MAX_CACHE_SIZE);
    //private static final Cache cache = DummyCache.dummyCache;
    
    private final Filesystem<Record> filesystem;
    
    private final MultiFetcher multiFetcher;
    
    public JsonMultiFetchStorageResource(Filesystem<Record> filesystem) {

        this.filesystem = filesystem;
        this.multiFetcher = filesystem.getMultiFetcher();
    }
    
    @POST
    @Timed
    public Response post(MultiFetchRequest request) {

        try {
            MultiFetchResponse response = multiFetcher.loadData(request, filesystem, cache);
            return Response.ok(response).build();
        }
        catch (Exception e) {
            return Response.serverError().entity("Problem getting keys").build();
        }
    }
    
}
