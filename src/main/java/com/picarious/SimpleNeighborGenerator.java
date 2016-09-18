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
        return new NpzState(workingDirectory, corpusPathAndFile, rAnalyzer, corpus, firstFields);
    }

    @Override
    public State newStateFrom(State currentState) {
        NpzState npzState = (NpzState) currentState;

        String[] previousFields = npzState.getFields();
        String[] newFields;
        double rand = Math.random();
        if (rand <= 0.5) {
            newFields = newSibling(previousFields);
        } else {
            newFields = newChild(previousFields);
        }

        return new NpzState(workingDirectory,
                corpusPathAndFile,
                rAnalyzer,
                corpus,
                newFields);
    }

    private String[] newChild(String[] previousFields) {
        return copyFieldsAndFindNew(previousFields, previousFields.length);
    }

    private String[] newSibling(String[] previousFields) {
        return copyFieldsAndFindNew(previousFields, previousFields.length - 1);
    }

    private String[] copyFieldsAndFindNew(String[] previousFields, int numToCopy) {
        Set<String> old = new HashSet<>();
        String[] replacementFields = new String[previousFields.length];
        for (int i = 0; i < numToCopy; i++) {
            old.add(previousFields[i]);
            replacementFields[i] = previousFields[i];
        }
        ArrayList<String> unusedFields = new ArrayList<>();
        for (String field : fields) {
            if (!old.contains(field)) {
                unusedFields.add(field);
            }
        }
        Collections.shuffle(unusedFields);
        replacementFields[replacementFields.length - 1] = unusedFields.get(0);
        return replacementFields;
    }
}
