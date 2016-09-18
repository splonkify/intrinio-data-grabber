package com.picarious;

import com.picarious.sa.State;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;


public class NpzState implements State {

    private final String workingDirectory;
    private final String corpusPathAndFile;
    private final RAnalyzer rAnalyzer;
    private final Corpus corpus;
    private final Optional<NpzState> parent;
    private final String[] fields;
    private final Set<String> visited;
    private Optional<Integer> energy = Optional.empty();

    public NpzState(String workingDirectory, String corpusPathAndFile, RAnalyzer rAnalyzer, Corpus corpus, Optional<NpzState> parent, Set<String> visited, String[] fields) {
        this.workingDirectory = workingDirectory;
        this.corpusPathAndFile = corpusPathAndFile;
        this.rAnalyzer = rAnalyzer;
        this.corpus = corpus;
        this.parent = parent;
        this.visited = visited;
        this.fields = fields;
    }

    @Override
    public int energy() {
        if (!energy.isPresent()) {
            corpus.setFields(fields);
            try {
                FileWriter fileWriter = new FileWriter(corpusPathAndFile);
                corpus.writeHeader(fileWriter);
                corpus.writeRecords(fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }

            energy = Optional.of(rAnalyzer.analyze(corpusPathAndFile, workingDirectory, false));
        }
        return energy.get();
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(",");
        for(String field : fields) {
            stringJoiner.add(field);
        }
        return String.format("Failures = %s for fields: %s", energy.get(), stringJoiner.toString());
    }

    public Set<String> getVisited() {
        return visited;
    }

    public String[] getFields() {
        return fields;
    }

    public Optional<NpzState> getParent() {
        return parent;
    }
}
