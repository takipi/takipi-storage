package com.takipi.oss.storage;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class TakipiStorageConfiguration extends Configuration {

    @Valid
    @JsonProperty
    private FolderFs folderFs;

    public static class FolderFs {
        @NotEmpty
        private String folderPath;

        @Min(0)
        @Max(1)
        private double maxUsedStoragePercentage = 0.9;

        @JsonProperty
        public String getFolderPath() {
            return folderPath;
        }

        @JsonProperty
        public void setFolderPath(String folderPath) {
            this.folderPath = folderPath;
        }

        @JsonProperty
        public double getMaxUsedStoragePercentage() {
            return maxUsedStoragePercentage;
        }

        @JsonProperty
        public void setMaxUsedStoragePercentage(double maxUsedStoragePercentage) {
            this.maxUsedStoragePercentage = maxUsedStoragePercentage;
        }
    }

    @Valid
    @JsonProperty
    private S3Fs s3Fs;

    public static class S3Fs {
    
        @NotEmpty
        private String bucket;

        @NotEmpty
        private String pathPrefix;
    
        @NotNull
        @Valid
        private Credentials credentials;

        public static class Credentials {
    
            private String accessKey;

            private String secretKey;

            @JsonProperty
            public String getAccessKey() {
                return TakipiStorageConfigurationEnvResolver.resolveEnv(accessKey);
            }

            @JsonProperty
            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            @JsonProperty
            public String getSecretKey() {
                return TakipiStorageConfigurationEnvResolver.resolveEnv(secretKey);
            }

            @JsonProperty
            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }
        }

        @JsonProperty
        public String getBucket() {
            return TakipiStorageConfigurationEnvResolver.resolveEnv(bucket);
        }

        @JsonProperty
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
        
        @JsonProperty
        public String getPathPrefix() {
            return pathPrefix;
        }

        @JsonProperty
        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }
        
        @JsonProperty
        public Credentials getCredentials() {
            return credentials;
        }

        @JsonProperty
        public void setCredentials(Credentials credentials) {
            this.credentials = credentials;
        }
    }
    
    @JsonProperty
    private Multifetch multifetch;
    
    public static class Multifetch {
        
        private Integer concurrencyLevel;
        private Integer maxCacheSize;
        private Boolean enableCacheLogger;
        
        @JsonProperty
        public Integer getConcurrencyLevel() {
            return concurrencyLevel;
        }
        
        @JsonProperty
        public void setConcurrencyLevel(Integer concurrencyLevel) {
            this.concurrencyLevel = concurrencyLevel;
        }
        
        @JsonProperty
        public Integer getMaxCacheSize() {
            return maxCacheSize;
        }
        
        @JsonProperty
        public void setMaxCacheSize(Integer maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        @JsonProperty
        public Boolean getEnableCacheLogger() {
            return enableCacheLogger;
        }

        @JsonProperty
        public void setEnableCacheLogger(Boolean enableCacheLogger) {
            this.enableCacheLogger = enableCacheLogger;
        }
    }
    
    private boolean enableCors;

    @NotEmpty
    private String corsOrigins;

    @JsonProperty
    public boolean isEnableCors() {
        return enableCors;
    }

    @JsonProperty
    public void setEnableCors(boolean enableCors) {
        this.enableCors = enableCors;
    }


    @JsonProperty
    public String getCorsOrigins() {
        return corsOrigins;
    }

    @JsonProperty
    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    @JsonProperty
    public FolderFs getFolderFs() {
        return folderFs;
    }

    public boolean hasFolderFs() {
        return folderFs != null;
    }

    @JsonProperty
    public void setFolderFs(FolderFs folderFs) {
        this.folderFs = folderFs;
    }

    @JsonProperty
    public S3Fs getS3Fs() {
        return s3Fs;
    }
    
    @JsonProperty
    public TakipiStorageConfiguration.Multifetch getMultifetch() {
        return multifetch;
    }
    
    @JsonProperty
    public void setS3Fs(S3Fs s3Fs) {
        this.s3Fs = s3Fs;
    }

    public boolean hasS3Fs() {
        return s3Fs != null;
    }
}
