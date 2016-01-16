package nu.nethome.home.impl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.io.File;

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
        Assert.assertEquals(actual, File.separator);
    }

    @Test
    public void testGetCompletePathNameUnixPathEmptyFilename() {
        String actual = HomeServer.getCompletePathName("/", "");
        Assert.assertEquals(File.separator, actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathValidFilename() {
        String actual = HomeServer.getCompletePathName("/var/log", "file.log");
        Assert.assertEquals("/var/log/file.log".replace("/", File.separator), actual);
    }

    @Ignore
    @Test
    public void testGetCompletePathNameUnixEmptyPathValidFilename() {
        String actual = HomeServer.getCompletePathName("", "/var/log/file.log");
        Assert.assertEquals("/var/log/file.log".replace("/", File.separator), actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathValidFilenameWithOtherAbsolutePath() {
        String actual = HomeServer.getCompletePathName("/var/log/nethome/", "/home/nethome/Termometer700.Log");
        Assert.assertEquals("/home/nethome/Termometer700.Log".replace("/", File.separator), actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathValidFilenameWithoutPath() {
        String actual = HomeServer.getCompletePathName("/var/log/nethome/", "Termometer700.Log");
        Assert.assertEquals("/var/log/nethome/Termometer700.Log".replace("/",File.separator), actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathAndFilenameSamePaths() {
        String actual = HomeServer.getCompletePathName("/var/log/nethome/", "/var/log/nethome/Termometer700.Log");
        Assert.assertEquals("/var/log/nethome/Termometer700.Log", actual);
    }

    @Test
    public void testGetCompletePathNameUnixPathAndFilenameSameRootPaths() {
        String actual = HomeServer.getCompletePathName("/", "/Termometer700.Log");
        Assert.assertEquals("/Termometer700.Log", actual);
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

    @Test
    public void testGetCompletePathNameWinPathAndFilenameSamePaths() {
        String actual = HomeServer.getCompletePathName("C:\\var\\log\\", "C:\\var\\log\\file.log");
        Assert.assertEquals("C:\\var\\log\\file.log", actual);
    }

    @Test
    public void testGetCompletePathNameWinPathAndFilenameSameRootPaths() {
        String actual = HomeServer.getCompletePathName("C:\\", "C:\\file.log");
        Assert.assertEquals("C:\\file.log", actual);
    }

}
