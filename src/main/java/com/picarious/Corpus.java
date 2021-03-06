package com.picarious;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Scope("prototype")
public class Corpus {
    static final String NO_FIELDS_SPECIFIED = "No fields specified";
    static final String NO_RECORDS_FOUND = "No records found";
    private List<String> fields;
    private final List<CorpusRecord> records;
    private boolean classified = true;

    public Corpus() {
        fields = new ArrayList<>();
        records = new ArrayList<>();
    }

    public void setFields(String... newFields) {
        fields = Arrays.stream(newFields).collect(Collectors.toList());
    }

    public boolean isClassified() {
        return classified;
    }

    public void setClassified(boolean classified) {
        this.classified = classified;
    }

    public Stream<String> fieldStream() {
        return fields.stream();
    }

    public void writeHeader(OutputStreamWriter writer) {
        if (fields.isEmpty()) {
            throw new RuntimeException(NO_FIELDS_SPECIFIED);
        }
        String header = fieldStream().collect(Collectors.joining(",")) + "\n";
        try {
            if (classified) {
                writer.write("classification,");
            }
            writer.write(header);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeRecords(OutputStreamWriter writer) {
        if (fields.isEmpty()) {
            throw new RuntimeException(NO_FIELDS_SPECIFIED);
        }
        if (records.isEmpty()) {
            throw new RuntimeException(NO_RECORDS_FOUND);
        }
        records.stream().forEach(
                corpusRecord -> {
                    try {
                        if (classified) {
                            writer.write(corpusRecord.getClassification() + ",");
                        }
                        writer.write(fieldStream().map(field -> corpusRecord.getValue(field)).collect(Collectors.joining(",")) + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public void addRecord(CorpusRecord corpusRecord) {
        records.add(corpusRecord);
    }
}
