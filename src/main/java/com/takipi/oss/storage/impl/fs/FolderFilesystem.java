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
    public byte[] getBytes(String folder, String key) throws IOException {
	File file = new File(buildPath(folder, key));
	
	return FileUtils.readFileToByteArray(file);
    }
    
    @Override
    public void putBytes(String folder, String key, byte[] bytes) throws IOException {
	File file = new File(buildPath(folder, key));
	
	FileUtils.writeByteArrayToFile(file, bytes);
    }
    
    @Override
    public String getJson(String folder, String key) throws IOException {
	File file = new File(buildPath(folder, key));
	
	return FileUtils.readFileToString(file);
    }
    
    @Override
    public void putJson(String folder, String key, String string) throws IOException {
	File file = new File(buildPath(folder, key));
	
	FileUtils.writeStringToFile(file, string);
    }
    
    private String buildPath(String folder, String key) {
	return root.getPath() + File.separator + folder + File.separator + key;
    }
}
