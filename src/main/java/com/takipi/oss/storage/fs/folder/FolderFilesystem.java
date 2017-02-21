package com.takipi.oss.storage.fs.folder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.takipi.oss.storage.fs.api.Filesystem;

public abstract class FolderFilesystem<T> extends FolderFilesystemHealth implements Filesystem<T> {
    public FolderFilesystem(String rootFolder, double maxUsedStoragePercentage) {
        super(rootFolder, maxUsedStoragePercentage);
    }

    public File getRoot() {
        return root;
    }

    @Override
    public InputStream get(T record) throws IOException {
        File file = new File(buildPath(record));

        return new FileInputStream(file);
    }

    @Override
    public void put(T record, InputStream is) throws IOException {
        File file = new File(buildPath(record));

        beforePut(file);

        OutputStream os = new FileOutputStream(file);

        IOUtils.copy(is, os);

        os.flush();
        os.close();
    }

    @Override
    public void delete(T record) throws IOException {
        File file = new File(buildPath(record));

        if (file.exists() && file.canWrite()) {
            if (!file.delete()) {
                throw new IOException("Problem deleting file " + file);
            }

            return;
        }

        throw new FileNotFoundException("File not exist " + file);
    }

    @Override
    public boolean exists(T record) throws IOException {
        File file = new File(buildPath(record));

        if (file.exists() && file.canRead()) {
            return true;
        }

        throw new FileNotFoundException();
    }

    @Override
    public long size(T record) throws IOException {
        File file = new File(buildPath(record));

        if (file.exists() && file.canRead()) {
            return file.length();
        }

        throw new FileNotFoundException();
    }
    
    protected void beforePut(File file) {
        file.getParentFile().mkdirs();
    }
    
    protected abstract String buildPath(T record);
}
