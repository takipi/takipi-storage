package com.takipi.oss.storage.main;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class StorageServerConfiguration extends Configuration {
    @NotEmpty
    private String folderPath;

    @JsonProperty
    public String getFolderPath() {
        return folderPath;
    }

    @JsonProperty
    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}