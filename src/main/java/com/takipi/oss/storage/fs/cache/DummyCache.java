package com.takipi.oss.storage.fs.cache;

public class DummyCache implements Cache
{
	public static DummyCache dummyCache = new DummyCache();
	
	@Override
	public String get(String key) {
		return null;
	}
	
	@Override
	public String put(String key, String value) {
		return null;
	}
}
