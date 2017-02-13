package com.takipi.oss.storage.fs.folder.simple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Predicate;
import com.takipi.oss.storage.data.simple.SimpleSearchResponse;
import com.takipi.oss.storage.fs.api.SearchRequest;
import com.takipi.oss.storage.fs.api.SearchResult;
import com.takipi.oss.storage.fs.folder.FolderFilesystem;
import com.takipi.oss.storage.helper.FilesystemUtil;
import com.takipi.oss.storage.resources.fs.JsonSimpleSearchStorageResource;

import javax.ws.rs.core.Response;

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
        return FilesystemUtil.fixPath(value);
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
        String data = FilesystemUtil.read(this, relFSPath, searchRequest.getEncodingType());

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
