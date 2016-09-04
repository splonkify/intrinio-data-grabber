package com.picarious;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.inject.Provider;
import java.io.FileWriter;


@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Autowired
    Provider<Corpus> corpusProvider;
    @Autowired
    Provider<CorpusRecord> corpusRecordProvider;

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    @Bean
    public CommandLineRunner run(StandardizedFinancials standardizedFinancials) throws Exception {
        return args -> {
            CorpusRecord corpusRecord = corpusRecordProvider.get();
            Corpus corpus = corpusProvider.get();

            standardizedFinancials.statements(corpusRecord);
            String fileName = "/Users/kgiles/R-projects/intrinio/corpus.csv";
            FileWriter fileWriter = new FileWriter(fileName);
            corpus.writeHeader(fileWriter);
            corpusRecord.toFile(fileWriter);
            fileWriter.close();
        };
    }
}
