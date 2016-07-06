package com.takipi.oss.storage.fs.folder.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Predicate;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.SearchRequest;
import com.takipi.oss.storage.fs.api.SearchResult;
import com.takipi.oss.storage.fs.folder.FolderFilesystem;
import com.takipi.oss.storage.helper.FilesystemUtil;

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

    @Override
    public SearchResult search(SearchRequest searchRequest) throws IOException {
        File searchRoot = new File(getRoot(), FilesystemUtil.fixPath(searchRequest.getBaseSearchPath()));

        ResourceFileCallback fileCallback = new ResourceFileCallback(searchRequest.getName(), searchRequest.shouldPreventDuplicates());
        FilesystemUtil.listFilesRecursively(searchRoot, fileCallback);
        File result = fileCallback.getFoundFile();

        if (result == null) {
            return null;
        }

        String relFSPath = result.getAbsolutePath().replace(getRoot().getAbsolutePath(), "");
        String data = FilesystemUtil.encode(new FileInputStream(relFSPath), searchRequest.getEncodingType());

        if (data == null) {
            return null;
        }

        return new SimpleSearchResponse(data, relFSPath.replace(searchRequest.getName(), ""));
    }

    private static class ResourceFileCallback implements Predicate<File> {
        private final String resourceName;
        private final boolean preventDuplicates;

        private File foundFile;

        protected ResourceFileCallback(String resourceName, boolean preventDuplicates)
        {
            this.resourceName = resourceName;
            this.preventDuplicates = preventDuplicates;

            this.foundFile = null;
        }

        @Override
        public boolean apply(File file)
        {
            if (!resourceName.equals(file.getName()))
            {
                return false;
            }

            if ((preventDuplicates) &&
                (foundFile != null))
            {
                foundFile = null; // never find more than one result if preventing duplicates
                return true;
            }

            foundFile = file;

            return !preventDuplicates; // if we don't prevent duplicates, we stop right now
        }

        public File getFoundFile()
        {
            return foundFile;
        }
    }
}
