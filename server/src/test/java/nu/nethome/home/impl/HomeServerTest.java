package nu.nethome.home.impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import org.hamcrest.BaseMatcher.*;
import org.hamcrest.Matchers.*;

public class HomeServerTest {

    @Test
    public void testGetCompletePathNameHasNullParams() {
        String actual = HomeServer.getCompletePathName(null, null);
        Assert.assertEquals("", actual);
    }

    @Test
    public void testGetCompletePathNameHasEmptyParams() {
        String actual = HomeServer.getCompletePathName("", "");
        Assert.assertEquals("", actual);
    }

    @Test
    public void testGetCompletePathNameEmptyPathNullFilename() {
        String actual = HomeServer.getCompletePathName("", null);
        Assert.assertEquals("", actual);
    }

    @Test
    public void testGetCompletePathNameNullPathEmptyFilename() {
        String actual = HomeServer.getCompletePathName("", null);
        Assert.assertEquals("", actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathNullFilename() {
        String actual = HomeServer.getCompletePathName("/", null);
        Assert.assertEquals("/", actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathEmptyFilename() {
        String actual = HomeServer.getCompletePathName("/", "");
        Assert.assertEquals("/", actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathValidFilename() {
        String actual = HomeServer.getCompletePathName("/var/log", "file.log");
        Assert.assertEquals("/var/log" + File.separator + "file.log", actual);
    }

    @Test
    public void testGetCompletePathNameUnixEmptyPathValidFilename() {
        String actual = HomeServer.getCompletePathName("", "/var/log/file.log");
        Assert.assertEquals("/var/log" + File.separator + "file.log", actual);
    }

    @Test
    public void testGetCompletePathNameWinPathNullFilename() {
        String actual = HomeServer.getCompletePathName("\\", null);
        Assert.assertEquals("\\", actual);
    }

    @Test
    public void testGetCompletePathNameWinPathEmptyFilename() {
        String actual = HomeServer.getCompletePathName("C:\\", "");
        Assert.assertEquals("C:\\", actual);
    }

    @Test
    public void testGetCompletePathNameWinPathValidFilename() {
        String actual = HomeServer.getCompletePathName("C:\\var\\log", "file.log");
        Assert.assertEquals("C:\\var\\log" + File.separator + "file.log", actual);
    }

    @Test
    public void testGetCompletePathNameWinEmptyPathValidFilename() {
        String actual = HomeServer.getCompletePathName("", "C:\\var\\log\\file.log");
        Assert.assertEquals("C:\\var\\log\\file.log", actual);
    }

}
