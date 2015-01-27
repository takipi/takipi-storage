package com.takipi.oss.storage.fs.folder;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.io.Files;
import com.takipi.oss.storage.fs.Record;
import com.takipi.oss.storage.fs.api.Filesystem;

public class FolderFilesystemTest {

    @SuppressWarnings("unused")
    @Test
    public void testRootFolderIsValid() {
        try {
            File tempRoot = newTempFolderFile();

            new FolderFilesystem(tempRoot.getPath(), 0.0);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testRootFolderIsInvalid() {
        try {
            new FolderFilesystem("//:/", 0.0);
            fail();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testRootFolderMaxUsedStorageValid() {
        try {
            File tempRoot = newTempFolderFile();

            new FolderFilesystem(tempRoot.getPath(), 0.95);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testRootFolderMaxUsedStorageBelowZero() {
        try {
            File tempRoot = newTempFolderFile();

            new FolderFilesystem(tempRoot.getPath(), -1);
            fail();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testRootFolderMaxUsedStorageAboveOne() {
        try {
            File tempRoot = newTempFolderFile();
            new FolderFilesystem(tempRoot.getPath(), 1.1);
            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testPutGetRecord() {
        try {
            boolean pass = putGetRecord(newStubBytes());
            assertTrue(pass);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testPutGetEmptyBytesRecord() {
        try {
            boolean pass = putGetRecord(newEmptyBytes());
            assertTrue(pass);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private boolean putGetRecord(byte[] bytes) throws IOException {
        Filesystem fs = newValidFolderFilesystem();
        InputStream is = new ByteArrayInputStream(bytes);

        Record record = newStubRecord();

        fs.put(record, is);

        InputStream respIs = fs.get(record);

        byte[] respBytes = IOUtils.toByteArray(respIs);

        return Arrays.equals(bytes, respBytes);
    }

    @Test
    public void testGetNonexistsRecord() {
        try {
            Filesystem fs = newValidFolderFilesystem();

            fs.get(newStubRecord());

            fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testDeleteRecord() {
        try {
            Filesystem fs = newValidFolderFilesystem();

            Record record = newStubRecord();

            fs.put(record, new ByteArrayInputStream(newStubBytes()));
            fs.delete(record);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDeleteNonexistRecord() {
        try {
            Filesystem fs = newValidFolderFilesystem();

            Record record = newStubRecord();

            fs.delete(record);
            fail();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testContainsRecord() {
        try {
            Filesystem fs = newValidFolderFilesystem();

            Record record = newStubRecord();
            fs.put(record, new ByteArrayInputStream(newStubBytes()));
            fs.exists(record);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testContainsNonexistRecord() {
        try {
            Filesystem fs = newValidFolderFilesystem();

            Record record = newStubRecord();

            fs.exists(record);
            fail();
        } catch (Exception e) {
        }
    }

    private File newTempFolderFile() {
        File file = Files.createTempDir();
        file.deleteOnExit();
        return file;
    }

    private Filesystem newValidFolderFilesystem() {
        File temp = newTempFolderFile();
        return new FolderFilesystem(temp.getPath(), 0.99);
    }

    private Record newStubRecord() {
        return new Record("temp", "temp", "temp");
    }

    private byte[] newStubBytes() {
        return "STUB".getBytes(Charset.forName("UTF-8"));
    }

    private byte[] newEmptyBytes() {
        return new byte[0];
    }
}
