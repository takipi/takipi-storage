package com.takipi.oss.storage.helper;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.fs.BaseRecord;
import com.takipi.oss.storage.fs.api.Filesystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesystemUtil {
  private static final Logger logger = LoggerFactory.getLogger(FilesystemUtil.class);
  
  public static String fixPath(String path) {
    return path.replace("/", File.separator).replace("\\", File.separator);
  }
  
    public static <T extends BaseRecord> String read(Filesystem<T> fs, T record, EncodingType encodingType) {
      InputStream is = null;

      try {
        is = fs.get(record);
        return encode(is, encodingType);
      } catch (IOException e) {
        return null;
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
          }
        }
      }
  }
  
  public static String encode(InputStream is, EncodingType type) throws IOException {
    switch (type) {
      case PLAIN:
      case JSON:
        return IOUtils.toString(is);
      case BINARY:
        throw new UnsupportedOperationException("not yet implemented");
    } 
    throw new IllegalArgumentException("problem encoding - " + type);
  }
  
  public static File listFilesRecursively(File baseFolder, Predicate<File> callback) {
    try {
      Set<File> seenFolders = new HashSet<>();
      Deque<File> pendingFolders = new LinkedList<>();
      pendingFolders.add(baseFolder);
      while (!pendingFolders.isEmpty()) {
        File folder = pendingFolders.pop();
        if (seenFolders.contains(folder))
          continue; 
        seenFolders.add(folder);
        try {
          if (!folder.exists() || !folder.canRead())
            continue; 
          if (!folder.isDirectory())
            continue; 
          File[] files = folder.listFiles();
          if (files == null)
            continue; 
          for (File file : files) {
            if (file.isDirectory()) {
              pendingFolders.add(file);
            } else if (callback.test(file)) {
              return file;
            } 
          } 
        } catch (Exception e) {
          logger.error("Error accessing folder {}.", folder);
        } 
      } 
      return null;
    } catch (Exception e) {
      logger.error("Error searcing in {}.", baseFolder);
      return null;
    } 
  }
}

