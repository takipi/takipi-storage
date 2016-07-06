package com.takipi.oss.storage.fs.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.api.SearchRequest;
import com.takipi.oss.storage.fs.api.SearchResult;
import com.takipi.oss.storage.fs.folder.simple.SimpleFilesystem;
import com.takipi.oss.storage.helper.FilesystemUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class S3Filesystem<T extends Record> implements Filesystem<T> {

    private final AmazonS3 amazonS3;
    private final String bucket;

    public S3Filesystem(AmazonS3 amazonS3, String bucket) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
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
        listObjectsRequest.setPrefix(searchRequest.getBaseSearchPath());
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

    private String keyOf(T record) {
        return record.getServiceId() + "/" + record.getType() + "/" + record.getKey();
    }

}
