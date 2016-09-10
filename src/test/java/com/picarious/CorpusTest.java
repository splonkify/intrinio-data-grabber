package com.picarious;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.mockito.Mockito.*;

public class CorpusTest {
    @InjectMocks
    Corpus systemUnderTest;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        systemUnderTest = new Corpus();
    }

    @Test
    public void writeHeaderWithNoFieldsThrowsException() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Corpus.NO_FIELDS_SPECIFIED);
        OutputStreamWriter writer = mock(OutputStreamWriter.class);

        systemUnderTest.writeHeader(writer);
    }

    @Test
    public void writeHeaderHappyPath() throws Exception {
        OutputStreamWriter writer = mock(OutputStreamWriter.class);
        systemUnderTest.setFields("A", "B", "C");

        systemUnderTest.writeHeader(writer);

        verify(writer).write("A,B,C\n");
    }

    @Test
    public void writeRecordsWithNoFieldsThrowsException() throws IOException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Corpus.NO_FIELDS_SPECIFIED);
        OutputStreamWriter writer = mock(OutputStreamWriter.class);

        systemUnderTest.writeRecords(writer);
    }

    @Test
    public void writeRecordsWithNoRecordsThrowsException() throws IOException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage(Corpus.NO_RECORDS_FOUND);
        OutputStreamWriter writer = mock(OutputStreamWriter.class);
        systemUnderTest.setFields("A", "B", "C");

        systemUnderTest.writeRecords(writer);
    }

    @Test
    public void writeRecordsHappyPath() throws IOException {
        String valueA = "0.1";
        String valueB = "0.2";
        CorpusRecord corpusRecord = mock(CorpusRecord.class);
        when(corpusRecord.getValue("A")).thenReturn(valueA);
        when(corpusRecord.getValue("B")).thenReturn(valueB);
        OutputStreamWriter writer = mock(OutputStreamWriter.class);
        systemUnderTest.setFields("A", "B");
        systemUnderTest.addRecord(corpusRecord);

        systemUnderTest.writeRecords(writer);

        verify(writer).write(valueA + "," + valueB + "\n");
    }


}