package com.picarious;

import com.picarious.intrinio.TagName;
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
import java.io.FileWriter;


@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Autowired
    Provider<Corpus> corpusProvider;

    @Value("${corpus.pathAndFile}")
    String corpusPathAndFile;

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
            corpus.addFields(TagName.BASICEPS, TagName.DELTAOPERATINGCAPITAL, TagName.LONGTERMDEBT);
            FileWriter fileWriter = new FileWriter(corpusPathAndFile);
            corpus.writeHeader(fileWriter);
            corpus.writeRecords(fileWriter);
            fileWriter.close();
        };
    }
}
