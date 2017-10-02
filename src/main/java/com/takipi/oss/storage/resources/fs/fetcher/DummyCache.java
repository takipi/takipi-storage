package com.takipi.oss.storage.resources.fs.fetcher;

public class DummyCache implements Cache
{
	@Override
	public String get(String key) {
		return null;
	}
	
	@Override
	public String put(String key, String value) {
		return null;
	}
}
