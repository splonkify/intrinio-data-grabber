package com.picarious;

import com.picarious.intrinio.FundamentalsDatum;
import com.picarious.sa.Annealer;
import com.picarious.sa.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Autowired
    Provider<Corpus> corpusProvider;

    @Autowired
    Provider<SimpleNeighborGenerator> simpleNeighborGeneratorProvider;

    @Autowired
    Provider<RandomNeighborGenerator> randomNeighborGeneratorProvider;

    @Autowired
    Annealer annealer;

    @Autowired
    RAnalyzer rAnalyzer;

    @Autowired
    RClassifier rClassifier;

    @Autowired
    RecentFilingFinder recentFilingFinder;

    @Value("${corpus.pathAndFile}")
    String corpusPathAndFile;

    @Value("${corpus.test.pathAndFile}")
    String corpusTestPathAndFile;

    @Value("${corpus.fields}")
    String corpusFields;

    @Value("${working.directory}")
    String workingDirectory;

    @Value("${mission}")
    String mission;

    @Value("${corpus.fixedFields}")
    String fixedFields;

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public CommandLineRunner run(CorpusRecordBuilder corpusRecordBuilder) throws Exception {
        return args -> {

            if (mission.equals("SearchAnnealRandom")) {
                RandomNeighborGenerator randomNeighborGenerator = randomNeighborGeneratorProvider.get();
                State finalState = annealer.search(randomNeighborGenerator);
                log.info(finalState.toString());
                saveAndAnalyze(randomNeighborGenerator.getCorpus(), ((NpzState) finalState).getFields());
            } else if (mission.equals("SearchAnnealSimple")) {
                SimpleNeighborGenerator simpleNeighborGenerator = simpleNeighborGeneratorProvider.get();
                State finalState = annealer.search(simpleNeighborGenerator);
                log.info(finalState.toString());
                saveAndAnalyze(simpleNeighborGenerator.getCorpus(), ((NpzState) finalState).getFields());

            } else if (mission.equals("SearchFirstTwo")) {
                Corpus corpus = corpusProvider.get();
                corpusRecordBuilder.build(corpus);
                List<String> fields = new ArrayList<>();
                Scanner scanner = new Scanner(new File(workingDirectory + "fields.txt"));
                while (scanner.hasNextLine()) {
                    fields.add(scanner.nextLine());
                }
                scanner.close();

                for (String field : fields) {
                    Iterator<String> fieldIter = fields.iterator();
                    while (fieldIter.hasNext()) {
                        String extraField = fieldIter.next();
                        if (!extraField.equals(field)) {
                            saveAndAnalyze(corpus, field, extraField);
                        }
                    }
                }
            } else if (mission.equals("SearchOneNew")) {
                Corpus corpus = corpusProvider.get();
                corpusRecordBuilder.build(corpus);
                List<String> fields = new ArrayList<>();
                Scanner scanner = new Scanner(new File(workingDirectory + "fields.txt"));
                while (scanner.hasNextLine()) {
                    fields.add(scanner.nextLine());
                }
                scanner.close();

                Set<String> fixedFieldsSet = new HashSet<>();
                fixedFieldsSet.addAll(Arrays.asList(fixedFields.split(",")));
                Iterator<String> fieldIter = fields.iterator();
                while (fieldIter.hasNext()) {
                    String extraField = fieldIter.next();
                    if (!fixedFieldsSet.contains(extraField)) {
                        ArrayList<String> corpusFields = new ArrayList<>();
                        corpusFields.addAll(fixedFieldsSet);
                        corpusFields.add(extraField);
                        saveAndAnalyze(corpus, corpusFields.toArray(new String[0]));
                    }
                }
            } else if (mission.equals("AnalyzeFields")) {
                Corpus corpus = corpusProvider.get();
                corpusRecordBuilder.build(corpus);
                saveAndAnalyze(corpus, corpusFields.split(","));
            } else {
                Corpus training = corpusProvider.get();
                corpusRecordBuilder.build(training);
                saveCorpus(corpusPathAndFile, training, corpusFields.split(","));

                Corpus testing = corpusProvider.get();
                testing.setClassified(false);
                List<FundamentalsDatum> fundamentals = recentFilingFinder.findFundamentalsOrderedByFilingDate();
                for (int i = 0; i < 10; i++) {
                    FundamentalsDatum fd = fundamentals.get(i);
                    log.info(fd.toString());
                    corpusRecordBuilder.addRecord(testing, fd.getTicker(), fd);
                }
                saveCorpus(corpusTestPathAndFile, testing, corpusFields.split(","));

                rClassifier.classify(corpusPathAndFile, corpusTestPathAndFile, workingDirectory, false);
            }

        };
    }

    private void saveAndAnalyze(Corpus corpus, String... fields) throws IOException {
        saveCorpus(corpusPathAndFile, corpus, fields);

        int failures = rAnalyzer.analyze(corpusPathAndFile, workingDirectory, true);
        log.info("failures = " + failures);
    }

    private void saveCorpus(String path, Corpus corpus, String[] fields) throws IOException {
        corpus.setFields(fields);
        FileWriter fileWriter = new FileWriter(path);
        corpus.writeHeader(fileWriter);
        corpus.writeRecords(fileWriter);
        fileWriter.close();
    }
}
