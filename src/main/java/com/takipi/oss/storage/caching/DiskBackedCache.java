package com.takipi.oss.storage.caching;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import com.takipi.oss.storage.fs.concurrent.SimpleStopWatch;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskBackedCache extends CacheDelegator {
    private static final Logger logger = LoggerFactory.getLogger(DiskBackedCache.class);

    private static final HashLock fileAccessLock = new HashLock();
    private static final int MAX_LOGGED_ERRORS = 10;
    private static volatile boolean cleanupInProgress = false;

    private final File rootFile;

    private final long totalDiskSpace;
    private final long maxDiskSpace;

    private final double cleanupPercentage;

    public DiskBackedCache(File rootFile, double maxDiskUsage, double cleanupPercentage) {
        super(null);

        this.rootFile = rootFile;
        this.rootFile.mkdirs();

        this.totalDiskSpace = rootFile.getTotalSpace();
        this.maxDiskSpace = (long) Math.floor(maxDiskUsage * this.totalDiskSpace);
        this.cleanupPercentage = cleanupPercentage;
    }

    @Override
    public String toString() {
        return "Disk Cache (path: " + rootFile.getAbsolutePath() +
                ") (max: " + maxDiskSpace +
                ") (clean: " + cleanupPercentage + ") -> " + super.toString();
    }

    @Override
    public <V> SerializableCacheValue<V> get(String key, SerializableCacheValue<V> result) {
        try {
            return internalGet(key, result);
        }
        catch (Exception e) {
            logger.error("Error getting from disk cache {}", key, e);
            return result;
        }
    }

    public <V> SerializableCacheValue<V> internalGet(String key, SerializableCacheValue<V> result) throws Exception {
        File file = keyToFile(key);

        byte[] data = null;

        synchronized (fileAccessLock.get(file)) {
            if (!file.canRead()) {
                return result;
            }

            data = FileUtils.readFileToByteArray(file);
            file.setLastModified(System.currentTimeMillis());
        }

        if (data != null) {
            result.setSerializedValue(this, data);
        }

        return result;
    }

    @Override
    public <V> SerializableCacheValue<V> put(String key, SerializableCacheValue<V> value, boolean overwrite) {
        try {
            return internalPut(key, value, overwrite);
        }
        catch (Exception e) {
            logger.error("Error putting to disk cache {}", key, e);
            return value;
        }
    }

    public <V> SerializableCacheValue<V> internalPut(String key, SerializableCacheValue<V> value, boolean overwrite)
            throws Exception {
        File file = keyToFile(key);

        if (!checkDiskUsage(true)) {
            cleanup();

            if (!checkDiskUsage(true)) {
                return value;
            }
        }

        byte[] data = value.serialize(key);

        if (data == null) {
            return value;
        }

        synchronized (fileAccessLock.get(file)) {
            if ((!overwrite) &&
                    (file.canRead())) {
                file.setLastModified(System.currentTimeMillis());
                return value;
            }

            checkDiskUsage(false);

            FileUtils.writeByteArrayToFile(file, data);

            if ((!file.setReadable(true, false)) ||
                    (!file.setWritable(true, false))) {
                throw new IllegalStateException("Unable to set read/write permissions for local file: " + file.getAbsolutePath());
            }
        }

        value.setUpdater(this);

        return value;
    }

    public <T> T use(String key, CacheFileCallback<T> callback) {
        File file = keyToFile(key);

        if (!file.canRead()) {
            return null;
        }

        synchronized (fileAccessLock.get(file)) {
            if (!file.canRead()) {
                return null;
            }

            return callback.run(file);
        }
    }

    private void cleanup() {
        if (cleanupPercentage == 0.0) {
            logger.info("Disk cleanup is disabled.");
            return;
        }

        if (cleanupInProgress) {
            logger.info("Disk cleanup is already in progress; skipping.");
            return;
        }

        cleanupInProgress = true;

        logger.info("Starting disk cleanup for {} ({}%).", this, (int) (cleanupPercentage * 100));

        SimpleStopWatch stopwatch = new SimpleStopWatch();
		
        try {
            String[] files = rootFile.list();

            if (files == null) {
                return;
            }

            logger.info("Sorting {} files by last-modified date.", files.length);

            Arrays.sort(files, new Comparator<String>() {
                @Override
                public int compare(String filename1, String filename2) {
                    long date1 = new File(filename1).lastModified();
                    long date2 = new File(filename2).lastModified();

                    return Long.valueOf(date1).compareTo(Long.valueOf(date2));
                }
            });

            int deleteCounter = (int) Math.floor(files.length * cleanupPercentage);

            logger.info("About to attempt to delete {} old files.", deleteCounter);

            int errorCounter = 0;
            int successCounter = 0;
            long sizeCounter = 0l;

            for (int i = 0; i < deleteCounter; i++) {
                try {
                    File file = new File(rootFile, files[i]);

                    synchronized (fileAccessLock.get(file)) {
                        if ((file.canWrite()) &&
                                (!file.isDirectory())) {
                            long fileSize = file.length();

                            FileUtils.forceDelete(file);

                            successCounter++;
                            sizeCounter += fileSize;
                        }
                    }
                }
                catch (Exception ex) {
                    errorCounter++;

                    if (errorCounter <= MAX_LOGGED_ERRORS) {
                        logger.error("Deleting {} from disk failed: {}: {}",
                                files[i], ex.getClass().getSimpleName(), ex.getMessage());
                    }
                }
            }

            if (successCounter == 0) {
                logger.warn("No files were deleted from: {}.", rootFile);
            }
            else {
                logger.info("A total of {} files ({} bytes) were deleted from: {}.",
                        successCounter, sizeCounter, rootFile);
            }

            if (errorCounter > 0) {
                logger.error("A total of {} files could not be deleted.", errorCounter);
            }
        }
        catch (Exception ex) {
            logger.error("Disk cleanup failed.", ex);
        }
        finally {
            logger.info("Disk cleanup completed in {} ms.", stopwatch.elapsed());

            cleanupInProgress = false;
        }
    }

    private boolean checkDiskUsage(boolean silent) {
        long curFreeSpace = rootFile.getUsableSpace();
        long minFreeSpace = totalDiskSpace - maxDiskSpace;

        if (curFreeSpace <= minFreeSpace) {
            if (silent) {
                return false;
            }
            else {
                throw new IllegalStateException("Max disk usage limit reached: " +
                        Double.toString((double) curFreeSpace / totalDiskSpace) +
                        "% left (limit: " +
                        Double.toString((double) minFreeSpace / totalDiskSpace) + ")");
            }
        }

        return true;
    }

    private File keyToFile(String key) {
        String validFileName = key.replace(File.separator, "");
        return new File(rootFile, validFileName);
    }

    public static interface CacheFileCallback<T> {
        T run(File file);
    }
}
