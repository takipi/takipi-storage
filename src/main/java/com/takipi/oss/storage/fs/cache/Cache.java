package com.takipi.oss.storage.fs.cache;

public interface Cache
{
	String get(String key);
	String put(String key, String value);
}
