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
    private final List<String> fields;

    public Corpus() {
        fields = new ArrayList<>();
    }

    public void addFields(String... newFields) {
        fields.addAll(Arrays.stream(newFields).collect(Collectors.toList()));
    }

    public Stream<String> fieldStream() {
        return fields.stream();
    }

    public void writeHeader(OutputStreamWriter writer) throws IOException {
        if (fields.isEmpty()) {
            throw new RuntimeException("No fields specified");
        }
        String header = fieldStream().collect(Collectors.joining(",")) + "\n";
        writer.write(header);
    }
}
