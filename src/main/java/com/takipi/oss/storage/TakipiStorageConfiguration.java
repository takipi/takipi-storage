package com.takipi.oss.storage;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class TakipiStorageConfiguration extends Configuration {
    @NotEmpty
    private String folderPath;

    @Min(0)
    @Max(1)
    private double maxUsedStoragePercentage = 0.9;

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
    public double getMaxUsedStoragePercentage() {
        return maxUsedStoragePercentage;
    }

    @JsonProperty
    public void setMaxUsedStoragePercentage(double maxUsedStoragePercentage) {
        this.maxUsedStoragePercentage = maxUsedStoragePercentage;
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
    public String getFolderPath() {
        return folderPath;
    }

    @JsonProperty
    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
