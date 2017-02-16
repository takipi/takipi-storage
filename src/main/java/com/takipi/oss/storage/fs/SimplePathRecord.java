package com.takipi.oss.storage.fs;

public class SimplePathRecord  implements BaseRecord {

    private final String path;

    private final String[] pathParts;

    public static SimplePathRecord newRecord(String path) {
       return new SimplePathRecord(path);
    }

    private SimplePathRecord(String path) {
        this.path = path;

        this.pathParts = path.split("/", 3);
    }

    public String getPath() {
       return path;
    }

    @Override
    public String getServiceId() {
        if (pathParts.length > 0)
        {
            return pathParts[0];
        }

        return "";
    }

    @Override
    public String getType() {
        if (pathParts.length > 1)
        {
            return pathParts[1];
        }

        return "";
    }

    @Override
    public String getKey() {
        if (pathParts.length > 2)
        {
            return pathParts[2];
        }

        return "";
    }
}

