package com.takipi.oss.storage.fs.folder.record;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.folder.FolderFilesystem;

public class RecordFilesystem extends FolderFilesystem<Record> {
    public RecordFilesystem(String rootFolder, double maxUsedStoragePercentage) {
        super(rootFolder, maxUsedStoragePercentage);
    }

    @Override
    protected String buildPath(Record record) {
        Path recordPath = Paths.get(root.getPath(), escape(record.getServiceId()), escape(record.getType()),
                escape(record.getKey()));

        return recordPath.toString();
    }

    protected String escape(String value) {
        return value.replace("..", "__").replace("/", "-").replace("\\", "-");
    }
}
