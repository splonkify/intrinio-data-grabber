package com.picarious;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RunnableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Scope("prototype")
public class Corpus {
    static final String NO_FIELDS_SPECIFIED = "No fields specified";
    static final String NO_RECORDS_FOUND = "No records found";
    private final List<String> fields;
    private final List<CorpusRecord> records;

    public Corpus() {
        fields = new ArrayList<>();
        records = new ArrayList<>();
    }

    public void addFields(String... newFields) {
        fields.addAll(Arrays.stream(newFields).collect(Collectors.toList()));
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
