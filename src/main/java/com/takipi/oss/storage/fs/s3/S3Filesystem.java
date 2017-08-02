package com.takipi.oss.storage.fs.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.SimplePathRecord;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.api.SearchRequest;
import com.takipi.oss.storage.fs.api.SearchResult;
import com.takipi.oss.storage.helper.FilesystemUtil;

public class S3Filesystem<T extends BaseRecord> implements Filesystem<T> {

    private final AmazonS3 amazonS3;
    private final String bucket;
    private final String pathPrefix;
    
    public S3Filesystem(AmazonS3 amazonS3, String bucket, String pathPrefix) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
        this.pathPrefix = pathPrefix;
    }

    @Override
    public void put(T record, InputStream is) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        amazonS3.putObject(bucket, keyOf(record), is, objectMetadata);
    }

    @Override
    public InputStream get(T record) throws IOException {
        return amazonS3.getObject(bucket, keyOf(record)).getObjectContent();
    }

    @Override
    public void delete(T record) throws IOException {
        amazonS3.deleteObject(bucket, keyOf(record));
    }

    @Override
    public boolean exists(T record) throws IOException {
        return amazonS3.doesObjectExist(bucket, keyOf(record));
    }

    @Override
    public long size(T record) throws IOException {
        return amazonS3.getObjectMetadata(bucket, keyOf(record)).getContentLength();
    }

    @Override
    public SearchResult search(SearchRequest searchRequest) throws IOException {

        // Start a prefix search
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucket);
        
        if (this.pathPrefix != null) {
            listObjectsRequest.setPrefix(this.pathPrefix + "/" + searchRequest.getBaseSearchPath());
        } else {
            listObjectsRequest.setPrefix(searchRequest.getBaseSearchPath());    
        }

        ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);

        // Just select the first object
        S3Object s3Object = null;
        for(S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            if(objectSummary.getKey().contains(searchRequest.getName())) {
                s3Object = amazonS3.getObject(bucket, objectSummary.getKey());
                break;
            }
        }

        if (s3Object == null) {
            return null;
        } else {
            String data = FilesystemUtil.encode(s3Object.getObjectContent(), searchRequest.getEncodingType());
            return new SimpleSearchResponse(data, searchRequest.getBaseSearchPath());
        }
    }

    @Override
    public boolean healthy() {
        return true;
    }

    @Override
    public BaseRecord pathToRecord(String path) {
        return SimplePathRecord.newRecord(path);
    }

    private String keyOf(T record) {
        if (this.pathPrefix != null) {
            return this.pathPrefix + File.separator + record.getPath();
        }
        
        return record.getPath();
    }

}
