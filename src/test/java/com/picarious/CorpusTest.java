package com.picarious;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.OutputStreamWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
public class CorpusTest {
    @InjectMocks
    Corpus systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new Corpus();
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = RuntimeException.class)
    public void writeHeaderWithNoFieldsThrowsException() throws Exception {
        OutputStreamWriter writer = mock(OutputStreamWriter.class);

        systemUnderTest.writeHeader(writer);
    }

    @Test
    public void writeHeaderHappyPath() throws Exception {
        OutputStreamWriter writer = mock(OutputStreamWriter.class);
        systemUnderTest.addFields("A", "B", "C");

        systemUnderTest.writeHeader(writer);

        verify(writer).write("A,B,C\n");
    }

}