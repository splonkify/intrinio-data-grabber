package com.picarious;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Autowired
    Provider<Corpus> corpusProvider;

    @Autowired
    RAnalyzer rAnalyzer;

    @Value("${corpus.pathAndFile}")
    String corpusPathAndFile;

    @Value("${corpus.fields}")
    String corpusFields;

    @Value("${working.directory}")
    String workingDirectory;

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
                    String field2 = fieldIter.next();
                    if (!field2.equals(field)) {
                        corpus.setFields(field, field2);
                        FileWriter fileWriter = new FileWriter(corpusPathAndFile);
                        corpus.writeHeader(fileWriter);
                        corpus.writeRecords(fileWriter);
                        fileWriter.close();

                        rAnalyzer.analyze(corpusPathAndFile, workingDirectory);
                    }
                }
            }

        };
    }
}
