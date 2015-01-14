package com.takipi.oss.storage.fs.folder;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

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
            Filesystem fs = newValidFolderFilesystem();
            byte[] bytes = newTestBytes();
            InputStream is = new ByteArrayInputStream(bytes);

            Record record = newStubRecord();

            fs.putRecord(record, is);

            InputStream respIs = fs.getRecord(newStubRecord());

            byte[] respBytes = IOUtils.toByteArray(respIs);

            assertArrayEquals(bytes, respBytes);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetNonexistsRecord() {
        try {
            Filesystem fs = newValidFolderFilesystem();

            fs.getRecord(newStubRecord());

            fail();
        } catch (Exception e) {
        }
    }

    private File newTempFolderFile() {
        File file = Files.createTempDir();
        file.deleteOnExit();
        return file;
    }

    private Filesystem newValidFolderFilesystem() throws IOException {
        File temp = newTempFolderFile();
        return new FolderFilesystem(temp.getPath(), 0.99);
    }

    private Record newStubRecord() {
        return new Record("temp", "temp", "temp");
    }

    private byte[] newTestBytes() {
        byte[] bytes = new byte[5];
        bytes[0] = 100;
        bytes[1] = 101;
        bytes[2] = 1;
        bytes[3] = -42;
        bytes[4] = 0;

        return bytes;
    }
}
