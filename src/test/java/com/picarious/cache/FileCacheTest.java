package com.picarious.cache;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class FileCacheTest {
    @InjectMocks
    FileCache systemUnderTest;

    @Mock
    FileHelper fileHelper;

    String cacheRoot = "/foo/bar";
    String suffix = "json";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        systemUnderTest = spy(new FileCache(cacheRoot, suffix, fileHelper));
    }

    @Test
    public void readReturnsNullWhenNotFound() {
        String key = "foo";

        String actual = systemUnderTest.read(key);

        assertNull(actual);
    }

    @Test
    public void readLooksInCacheRoot() {
        String key = "foo";

        String actual = systemUnderTest.read(key);

        verify(fileHelper).openFile(cacheRoot, key, suffix);
    }

    @Test
    public void readGetsLineFromFile() {
        String key = "foo";
        String line = "this is the line";
        File file = mock(File.class);
        when(fileHelper.openFile(cacheRoot, key, suffix)).thenReturn(file);
        doReturn(line).when(systemUnderTest).readLine(file);

        String actual = systemUnderTest.read(key);

        assertEquals(line, actual);
    }

    @Test
    public void writeGoesToCacheRoot() {
        String key = "foo";
        String line = "this is the line";
        doNothing().when(systemUnderTest).writeLine(null, line);

        systemUnderTest.write(key, line);

        verify(fileHelper).createFile(cacheRoot, key, suffix);
    }

    @Test
    public void writePutsLineInFile() {
        String key = "foo";
        String line = "this is the line";
        File file = mock(File.class);
        when(fileHelper.createFile(cacheRoot, key, suffix)).thenReturn(file);
        doNothing().when(systemUnderTest).writeLine(file, line);

        systemUnderTest.write(key, line);

        verify(systemUnderTest).writeLine(file, line);
    }


}
