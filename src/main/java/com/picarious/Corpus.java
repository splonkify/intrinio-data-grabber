package com.picarious;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.picarious.TagName.BASICEPS;
import static com.picarious.TagName.DELTAINCOME;
import static com.picarious.TagName.LONGTERMDEBT;

/**
 * Created by kgiles on 9/4/16.
 */
@Service
@Scope("prototype")
public class Corpus {
    private final List<String> fields;

    public Corpus() {
        fields = new ArrayList<>();
    }

    public void addFields(String ... newFields) {
        fields.addAll(Arrays.stream(newFields).collect(Collectors.toList()));
    }

    public Stream<String> fieldStream() {
        return fields.stream();
    }

    public void writeHeader(OutputStreamWriter writer) throws IOException {
        StringJoiner stringJoiner = new StringJoiner(",", "", "\n");
        stringJoiner.add(BASICEPS);
        stringJoiner.add(LONGTERMDEBT);
        stringJoiner.add(DELTAINCOME);

        writer.write(stringJoiner.toString());
    }
}
