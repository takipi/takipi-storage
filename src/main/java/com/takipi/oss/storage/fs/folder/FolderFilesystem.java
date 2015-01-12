package com.takipi.oss.storage.fs.folder;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.takipi.oss.storage.fs.api.Filesystem;

public class FolderFilesystem implements Filesystem {
    private final File root;
    private final double maxUsedStoragePercentage;

    public FolderFilesystem(String rootFolder, double maxUsedStoragePercentage) {
        this.root = new File(rootFolder);
        this.maxUsedStoragePercentage = maxUsedStoragePercentage;

        if (!healthy()) {
            throw new IllegalStateException("Problem with path " + rootFolder);
        }
    }

    @Override
    public boolean healthy() {
        return ((this.root.canRead()) && (this.root.canWrite()) && 
                ((this.root.getUsableSpace() / this.root.getTotalSpace()) <= maxUsedStoragePercentage));
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

    @Override
    public void delete(String key) throws IOException {
        File file = new File(buildPath(key));

        file.delete();
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
