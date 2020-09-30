package com.takipi.oss.storage;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TakipiStorageConfiguration {
  private static final String ENV_TAKIPI_ENABLE_CORS = "TAKIPI_ENABLE_CORS";
  
  private static final String ENV_TAKIPI_CORS_ORIGIN = "TAKIPI_CORS_ORIGIN";
  
  private static final String ENV_TAKIPI_MULTIFETCH_CONCURRENCY_LEVEL = "TAKIPI_MULTIFETCH_CONCURRENCY_LEVEL";
  
  private static final String ENV_TAKIPI_MULTIFETCH_MAX_BATCH_SIZE = "TAKIPI_MULTIFETCH_MAX_BATCH_SIZE";
  
  private static final String ENV_TAKIPI_S3_BUCKET = "TAKIPI_S3_BUCKET";
  
  private static final String ENV_TAKIPI_S3_PATH_PREFIX = "TAKIPI_S3_PATH_PREFIX";
  
  private static final Logger log = LoggerFactory.getLogger(TakipiStorageConfiguration.class);
  
  @Valid
  private FolderFs folderFs;
  
  public static class FolderFs {
    @NotEmpty
    private String folderPath = "/";
    
    @Min(0L)
    @Max(1L)
    private double maxUsedStoragePercentage = 0.9D;
    
    public String getFolderPath() {
      return this.folderPath;
    }
    
    public void setFolderPath(String folderPath) {
      this.folderPath = folderPath;
    }
    
    public double getMaxUsedStoragePercentage() {
      return this.maxUsedStoragePercentage;
    }
    
    public void setMaxUsedStoragePercentage(double maxUsedStoragePercentage) {
      this.maxUsedStoragePercentage = maxUsedStoragePercentage;
    }
  }
  
  @Valid
  private S3Fs s3Fs = new S3Fs();
  
  public static class S3Fs {
    @NotNull
    @NotEmpty
    private String bucket;
    
    @NotNull
    private String pathPrefix;
    
    @NotNull
    @Valid
    private Credentials credentials = new Credentials();
    
    public static class Credentials {
      private String accessKey;
      
      private String secretKey;
      
      public String getAccessKey() {
        return TakipiStorageConfigurationEnvResolver.resolveEnv(this.accessKey);
      }
      
      public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
      }
      
      public String getSecretKey() {
        return TakipiStorageConfigurationEnvResolver.resolveEnv(this.secretKey);
      }
      
      public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
      }
    }
    
    public String getBucket() {
      return TakipiStorageConfigurationEnvResolver.resolveEnv(this.bucket);
    }
    
    public void setBucket(String bucket) {
      this.bucket = bucket;
    }
    
    public String getPathPrefix() {
      return this.pathPrefix;
    }
    
    public void setPathPrefix(String pathPrefix) {
      this.pathPrefix = pathPrefix;
    }
    
    public Credentials getCredentials() {
      return this.credentials;
    }
    
    public void setCredentials(Credentials credentials) {
      this.credentials = credentials;
    }
  }
  
  private static Multifetch multifetch = new Multifetch();
  
  public static class Multifetch {
    private Integer concurrencyLevel;
    
    private Integer maxCacheSize;
    
    private Boolean enableCacheLogger;
    
    private Integer maxBatchSize;
    
    public Integer getConcurrencyLevel() {
      return this.concurrencyLevel;
    }
    
    public void setConcurrencyLevel(Integer concurrencyLevel) {
      this.concurrencyLevel = concurrencyLevel;
    }
    
    public Integer getMaxCacheSize() {
      return this.maxCacheSize;
    }
    
    public void setMaxCacheSize(Integer maxCacheSize) {
      this.maxCacheSize = maxCacheSize;
    }
    
    public Boolean getEnableCacheLogger() {
      return this.enableCacheLogger;
    }
    
    public void setEnableCacheLogger(Boolean enableCacheLogger) {
      this.enableCacheLogger = enableCacheLogger;
    }
    
    public Integer getMaxBatchSize() {
      return this.maxBatchSize;
    }
    
    public void setMaxBatchSize(Integer maxBatchSize) {
      this.maxBatchSize = maxBatchSize;
    }
  }
  
  private boolean enableCors = true;
  
  @NotEmpty
  private String corsOrigins = "*";
  
  public boolean isEnableCors() {
    return this.enableCors;
  }
  
  public void setEnableCors(boolean enableCors) {
    this.enableCors = enableCors;
  }
  
  public String getCorsOrigins() {
    return this.corsOrigins;
  }
  
  public void setCorsOrigins(String corsOrigins) {
    this.corsOrigins = corsOrigins;
  }
  
  public FolderFs getFolderFs() {
    return this.folderFs;
  }
  
  public boolean hasFolderFs() {
    return (this.folderFs != null);
  }
  
  public void setFolderFs(FolderFs folderFs) {
    this.folderFs = folderFs;
  }
  
  public S3Fs getS3Fs() {
    return this.s3Fs;
  }
  
  public Multifetch getMultifetch() {
    return multifetch;
  }
  
  public void setS3Fs(S3Fs s3Fs) {
    this.s3Fs = s3Fs;
  }
  
  public boolean hasS3Fs() {
    return (this.s3Fs != null);
  }
  
  public TakipiStorageConfiguration init() {
    multifetch.setEnableCacheLogger(Boolean.valueOf(false));
    multifetch.setMaxCacheSize(Integer.valueOf(0));
    try {
      multifetch.setConcurrencyLevel(Integer.valueOf(System.getenv("TAKIPI_MULTIFETCH_CONCURRENCY_LEVEL")));
    } catch (NumberFormatException e) {
      multifetch.setConcurrencyLevel(Integer.valueOf(0));
    } 
    try {
      multifetch.setMaxBatchSize(Integer.valueOf(System.getenv("TAKIPI_MULTIFETCH_MAX_BATCH_SIZE")));
    } catch (NumberFormatException e) {
      multifetch.setMaxBatchSize(Integer.valueOf(0));
    } 
    this.s3Fs.setBucket(System.getenv("TAKIPI_S3_BUCKET"));
    this.s3Fs.setPathPrefix(System.getenv("TAKIPI_S3_PATH_PREFIX"));
    String envEnCors = System.getenv("TAKIPI_ENABLE_CORS");
    if (envEnCors != null && !envEnCors.trim().equalsIgnoreCase("false")) {
      setEnableCors(true);
      String envCorsOri = System.getenv("TAKIPI_CORS_ORIGIN");
      if (envCorsOri != null) {
        setCorsOrigins(envCorsOri.toLowerCase().trim());
      } else {
        setCorsOrigins("*");
      } 
    } else {
      setEnableCors(false);
      setCorsOrigins("*");
    } 
    return this;
  }
}

