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
    public byte[] get(String folder, String key) throws IOException {
	File file = new File(buildPath(folder, key));
	
	return FileUtils.readFileToByteArray(file);
    }
    
    @Override
    public void put(String folder, String key, byte[] bytes) throws IOException {
	File file = new File(buildPath(folder, key));
	
	FileUtils.writeByteArrayToFile(file, bytes);
    }
    
    private String buildPath(String folder, String key) {
	return root.getPath() + File.separator + folder + File.separator + key;
    }
}
