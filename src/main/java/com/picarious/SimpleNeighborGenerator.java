package com.picarious;

import com.picarious.sa.NeighborGenerator;
import com.picarious.sa.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Service
@Scope("prototype")
public class SimpleNeighborGenerator implements NeighborGenerator {
    @Value("${working.directory}")
    String workingDirectory;

    @Value("${corpus.pathAndFile}")
    String corpusPathAndFile;

    @Autowired
    Provider<Corpus> corpusProvider;

    @Autowired
    CorpusRecordBuilder corpusRecordBuilder;

    @Autowired
    RAnalyzer rAnalyzer;

    private List<String> fields;
    private Corpus corpus;

    @PostConstruct
    public void init() {
        corpus = corpusProvider.get();
        corpusRecordBuilder.build(corpus);

        fields = new ArrayList<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(workingDirectory + "fields.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            fields.add(scanner.nextLine());
        }
        scanner.close();
    }

    @Override
    public State initialState() {
        Collections.shuffle(fields);
        String[] firstFields = new String[2];
        firstFields[0] = fields.get(0);
        firstFields[1] = fields.get(1);
        HashSet<String> visited = new HashSet<>();
        visited.add(fields.get(0));
        visited.add(fields.get(1));
        return new NpzState(workingDirectory, corpusPathAndFile, rAnalyzer, corpus, Optional.empty(), visited, firstFields);
    }

    @Override
    public State newStateFrom(State currentState) {
        NpzState npzState = (NpzState) currentState;

        Set<String> visited = npzState.getVisited();

        String[] previousFields = npzState.getFields();
        String[] newFields;
        Set<String> newVisited = new HashSet<>();
        double rand = Math.random();
        Optional<NpzState> parent;
        if (rand <= 0.50) {
            if (npzState.getParent().isPresent()) {
                return newStateFrom(npzState.getParent().get());
            } else {
                return initialState();
            }
        }else if (rand <= 0.75 && visited.size() < fields.size()) {
            newFields = newSibling(previousFields, visited);
            newVisited.addAll(visited);
            newVisited.add(newFields[newFields.length - 1]);
            parent = npzState.getParent();
        } else {
            newFields = newChild(previousFields, visited);
            newVisited.addAll(Arrays.asList(newFields));
            parent = Optional.of(npzState);
        }

        return new NpzState(workingDirectory,
                corpusPathAndFile,
                rAnalyzer,
                corpus,
                parent,
                newVisited,
                newFields);
    }

    public Corpus getCorpus() {
        return corpus;
    }

    private String[] newChild(String[] previousFields, Set<String> visited) {
        return copyFieldsAndFindNew(previousFields, previousFields.length, visited);
    }

    private String[] newSibling(String[] previousFields, Set<String> visited) {
        return copyFieldsAndFindNew(previousFields, previousFields.length - 1, visited);
    }

    private String[] copyFieldsAndFindNew(String[] previousFields, int numToCopy, Set<String> visited) {
        String[] replacementFields = new String[numToCopy + 1];
        for (int i = 0; i < numToCopy; i++) {
            replacementFields[i] = previousFields[i];
        }
        ArrayList<String> availableFields = new ArrayList<>();
        for (String field : fields) {
            if (!visited.contains(field)) {
                availableFields.add(field);
            }
        }
        Collections.shuffle(availableFields);
        replacementFields[replacementFields.length - 1] = availableFields.get(0);
        return replacementFields;
    }
}
