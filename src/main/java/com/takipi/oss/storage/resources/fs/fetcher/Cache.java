package com.takipi.oss.storage.resources.fs.fetcher;

public interface Cache
{
	String get(String key);
	String put(String key, String value);
}
