package com.takipi.oss.storage.impl.fs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.takipi.oss.storage.api.fs.Filesystem;

public class FolderFilesystem implements Filesystem {
    private final File root;
    
    public FolderFilesystem(String rootFolder) {
	this.root = new File(rootFolder);
	
	if (!this.root.canRead() || !this.root.canWrite())
	{
	    throw new IllegalStateException("Problem with path " + rootFolder);
	}
    }
    
    @Override
    public byte[] getBytes(String key) throws IOException {
	File file = new File(buildPath(key));
	
	return FileUtils.readFileToByteArray(file);
    }
    
    @Override
    public void putBytes(String key, byte[] bytes) throws IOException {
	File file = new File(buildPath(key));
	
	beforePut(file);
	
	FileUtils.writeByteArrayToFile(file, bytes);
    }
    
    @Override
    public String getJson(String key) throws IOException {
	File file = new File(buildPath(key));
	
	return FileUtils.readFileToString(file);
    }
    
    @Override
    public void putJson(String key, String string) throws IOException {
	File file = new File(buildPath(key));
	
	beforePut(file);
	
	FileUtils.writeStringToFile(file, string);
    }
    
    protected String buildPath(String key) {
	StringBuilder sb = new StringBuilder();
	sb.append(root);
	sb.append(File.separator);
	sb.append(key);
	
	return sb.toString();
    }
    
    protected void beforePut(@SuppressWarnings("unused") File file) {
	
    }
}
