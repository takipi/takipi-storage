package com.takipi.oss.storage.fs.folder;

import java.io.File;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashSubfolderFilesystem extends FolderFilesystem {
    private HashFunction func;

    public HashSubfolderFilesystem(String rootFolder) {
        super(rootFolder);

        func = Hashing.murmur3_32();
    }

    @Override
    protected String buildPath(String key) {
        String hashKey = hashKey(key);

        String path = super.buildPath(key);

        File pathParent = new File(path).getParentFile();

        StringBuilder sb = new StringBuilder();
        sb.append(pathParent);
        sb.append(File.separator);
        sb.append(hashKey);
        sb.append(File.separator);
        sb.append(key);

        return sb.toString();
    }

    @Override
    protected void beforePut(File file) {
        file.getParentFile().mkdirs();
    }

    private String hashKey(String key) {
        byte[] hashBytes = func.newHasher().putString(key, Charsets.UTF_8).hash().asBytes();

        StringBuilder sbResp = new StringBuilder();

        sbResp.append(Math.abs(hashBytes[0]));
        sbResp.append(File.separator);
        sbResp.append(Math.abs(hashBytes[1]));
        sbResp.append(File.separator);
        sbResp.append(Math.abs(hashBytes[2]));
        sbResp.append(File.separator);
        sbResp.append(Math.abs(hashBytes[3]));

        return sbResp.toString();
    }
}
