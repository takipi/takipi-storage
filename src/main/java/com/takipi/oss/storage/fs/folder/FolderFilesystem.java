package com.takipi.oss.storage.fs.folder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.takipi.oss.storage.fs.Record;
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
    public byte[] getBytes(Record record) throws IOException {
        File file = new File(buildPath(record));

        return FileUtils.readFileToByteArray(file);
    }

    @Override
    public void putBytes(Record record, byte[] bytes) throws IOException {
        File file = new File(buildPath(record));

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
    public void delete(Record record) throws IOException {
        File file = new File(buildPath(record));

        file.delete();
    }

    protected String buildPath(Record record) {
        Path recordPath = Paths.get(root.getPath(), record.getServiceId(), record.getType(), record.getServiceId());

        return recordPath.toString();
    }
    
    protected String buildPath(String key) {
        Path recordPath = Paths.get(root.getPath(), key);

        return recordPath.toString();
    }

    protected void beforePut(@SuppressWarnings("unused") File file) {

    }
}
