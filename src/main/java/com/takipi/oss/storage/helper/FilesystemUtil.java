package com.takipi.oss.storage.helper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.takipi.oss.storage.data.EncodingType;
import com.takipi.oss.storage.fs.api.Filesystem;

public class FilesystemUtil {
    public static<T> String read(Filesystem<T> fs, T record, EncodingType encodingType) {
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
                } catch (IOException e) {}
            }
        }
    }
    
    private static String encode(InputStream is, EncodingType type) throws IOException {
        switch (type) {
            case PLAIN:
            case JSON: {
                return IOUtils.toString(is);
            }
            case BINARY: {
                throw new UnsupportedOperationException("not yet implemented");
                // byte[] bytes = IOUtils.toByteArray(is);
                // Base64Coder.encode(bytes);
            }
        }

        throw new IllegalArgumentException("problem encoding - " + type);
    }
}
