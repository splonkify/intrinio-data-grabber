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
public class RandomNeighborGenerator implements NeighborGenerator {
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
        return new NpzState(workingDirectory,
                corpusPathAndFile,
                rAnalyzer,
                corpus,
                Optional.empty(),
                Collections.EMPTY_SET,
                randomFields());
    }

    @Override
    public State newStateFrom(State currentState) {
        return initialState();
    }

    private String[] randomFields() {
        int count = (int) Math.round(Math.random() * (fields.size() / 2)) + 2;
        Set<String> fieldSet = new HashSet<>();
        for (int i = 0; i < count; i++) {
            do {
                Collections.shuffle(fields);
            } while (fieldSet.contains(fields.get(0)));
            fieldSet.add(fields.get(0));
        }
        return fieldSet.toArray(new String[0]);
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
