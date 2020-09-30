package com.takipi.oss.storage.fs.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;
import com.takipi.oss.storage.fs.api.SearchRequest;
import com.takipi.oss.storage.fs.api.SearchResult;
import com.takipi.oss.storage.helper.FilesystemUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Filesystem<T extends BaseRecord> implements Filesystem<T> {
  private static final Logger logger = LoggerFactory.getLogger(S3Filesystem.class);
  
  private final AmazonS3 amazonS3;
  
  private final String bucket;
  
  private final String pathPrefix;
  
  public S3Filesystem(AmazonS3 amazonS3, String bucket, String pathPrefix) {
    this.amazonS3 = amazonS3;
    this.bucket = bucket;
    this.pathPrefix = pathPrefix;
  }
  
  public void put(T record, InputStream is) throws IOException {
    ObjectMetadata objectMetadata = new ObjectMetadata();
    this.amazonS3.putObject(this.bucket, keyOf(record), is, objectMetadata);
  }
  
  public InputStream get(T record) throws IOException {
    return (InputStream)this.amazonS3.getObject(this.bucket, keyOf(record)).getObjectContent();
  }
  
  public void delete(T record) throws IOException {
    this.amazonS3.deleteObject(this.bucket, keyOf(record));
  }
  
  public <U extends T> void deleteMulti(List<U> recordList) throws Exception {
    ArrayList<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<>();
    for (BaseRecord baseRecord : recordList)
      keysToDelete.add(new DeleteObjectsRequest.KeyVersion(keyOf((T)baseRecord))); 
    StringBuilder str = new StringBuilder();
    for (DeleteObjectsRequest.KeyVersion keyVer : keysToDelete)
      str.append(" " + keyVer.getKey()); 
    logger.info("keys to delete:" + str.toString());
    try {
      DeleteObjectsRequest multiObjectDeleteRequest = (new DeleteObjectsRequest(this.bucket)).withKeys(keysToDelete).withQuiet(false);
      this.amazonS3.deleteObjects(multiObjectDeleteRequest);
    } catch (MultiObjectDeleteException mode) {
      HashSet<String> keySet = new HashSet<>();
      mode.getDeletedObjects().forEach(a -> keySet.add(" " + a.getKey()));
      logger.info("deleted keys: " + keySet);
      keySet.clear();
      mode.getErrors().forEach(a -> keySet.add(" " + a.getKey() + " / " + a.getCode() + " / " + a.getMessage() + " ; "));
      logger.error("error deleting keys/code/message:" + keySet);
      throw mode;
    } catch (Exception e) {
      throw e;
    } 
  }
  
  public boolean exists(T record) throws IOException {
    return this.amazonS3.doesObjectExist(this.bucket, keyOf(record));
  }
  
  public long size(T record) throws IOException {
    return this.amazonS3.getObjectMetadata(this.bucket, keyOf(record)).getContentLength();
  }
  
  public SearchResult search(SearchRequest searchRequest) throws IOException {
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
    listObjectsRequest.setBucketName(this.bucket);
    if (this.pathPrefix != null) {
      listObjectsRequest.setPrefix(this.pathPrefix + "/" + searchRequest.getBaseSearchPath());
    } else {
      listObjectsRequest.setPrefix(searchRequest.getBaseSearchPath());
    } 
    ObjectListing objectListing = this.amazonS3.listObjects(listObjectsRequest);
    S3Object s3Object = null;
    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
      if (objectSummary.getKey().contains(searchRequest.getName())) {
        s3Object = this.amazonS3.getObject(this.bucket, objectSummary.getKey());
        break;
      } 
    } 
    if (s3Object == null)
      return null; 
    String data = FilesystemUtil.encode((InputStream)s3Object.getObjectContent(), searchRequest.getEncodingType());
    return (SearchResult)new SimpleSearchResponse(data, searchRequest.getBaseSearchPath());
  }
  
  public boolean healthy() {
    return true;
  }
  
  public BaseRecord pathToRecord(String path) {
    String[] strs = path.trim().split(File.separator, 3);
    if (strs.length == 3)
      return (BaseRecord)Record.newRecord(strs[0], strs[1], strs[2]); 
    return null;
  }
  
  private String keyOf(T record) {
    if (this.pathPrefix != null)
      return this.pathPrefix + File.separator + record.getPath(); 
    return record.getPath();
  }
}

