package com.takipi.oss.storage;

import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import de.spinscale.dropwizard.jobs.JobConfiguration;

public class TakipiStorageConfiguration extends Configuration implements JobConfiguration {
	@NotEmpty
	private String folderPath;

	@Min(0)
	@Max(1)
	private double maxUsedStoragePercentage = 0.9;

	private boolean enableCors;

	@NotEmpty
	private String corsOrigins;
	
	private int retentionPeriodDays;
	
	@JsonProperty("jobs")
	private Map<String, String> jobs;
	
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
	
	@JsonProperty
	public Map<String, String> getJobs() {
		return jobs;
	}
	
	@JsonProperty
	public void setJobs(Map<String, String> jobs) {
		this.jobs = jobs;
	}
	
	@JsonProperty
	public int getRetentionPeriodDays() {
		return retentionPeriodDays;
	}

	@JsonProperty
	public void setRetentionPeriodDays(int retentionPeriodDays) {
		this.retentionPeriodDays = retentionPeriodDays;
	}
}
