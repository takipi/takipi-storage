package com.takipi.oss.storage.fs.folder.simple;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.takipi.oss.storage.fs.folder.FolderFilesystem;

public class SimpleFilesystem extends FolderFilesystem<String> {
    public SimpleFilesystem(String rootFolder, double maxUsedStoragePercentage) {
        super(rootFolder, maxUsedStoragePercentage);
    }

    @Override
    protected String buildPath(String record) {
        Path recordPath = Paths.get(root.getPath(), escape(record));

        return recordPath.toString();
    }

    protected String escape(String value) {
        return value.replace("/", File.separator).replace("\\", File.separator);
    }
}
