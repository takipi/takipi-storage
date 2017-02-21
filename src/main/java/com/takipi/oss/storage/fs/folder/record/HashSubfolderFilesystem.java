package com.takipi.oss.storage.fs.folder.record;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.takipi.oss.storage.fs.Record;

public class HashSubfolderFilesystem extends RecordFilesystem {
    private HashFunction func;

    public HashSubfolderFilesystem(String rootFolder, double maxUsedStoragePercentage) {
        super(rootFolder, maxUsedStoragePercentage);

        func = Hashing.murmur3_32();
    }

    @Override
    protected String buildPath(Record record) {
        String key = record.getKey();

        String hashKey = hashKey(key);

        Path recordPath = Paths.get(root.getPath(), escape(record.getServiceId()), escape(record.getType()),
                hashKey, escape(key));

        return recordPath.toString();
    }

    private String hashKey(String key) {
        byte[] hashBytes = func.newHasher().putString(key, Charsets.UTF_8).hash().asBytes();

        StringBuilder sb = new StringBuilder();
        sb.append(Math.abs(hashBytes[0]));
        sb.append(File.separator);
        sb.append(Math.abs(hashBytes[2]));

        return sb.toString();
    }
}
