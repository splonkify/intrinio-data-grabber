package com.picarious;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picarious.intrinio.FinancialData;
import com.picarious.intrinio.JsonTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.IOException;

@Service
public class CorpusRecordBuilder {
    private static final Logger log = LoggerFactory.getLogger(CorpusRecordBuilder.class);

    private final Provider<CorpusRecord> corpusRecordProvider;

    @Autowired
    public CorpusRecordBuilder(Provider<CorpusRecord> corpusRecordProvider) {
        this.corpusRecordProvider = corpusRecordProvider;
    }

    public CorpusRecord build() {
        ObjectMapper mapper = new ObjectMapper();
        CorpusRecord corpusRecord = corpusRecordProvider.get();
        try {
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_BALANCE_SHEET), FinancialData.class));
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_CASH_FLOW_STATEMENT), FinancialData.class));
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_INCOME_STATEMENT), FinancialData.class));
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_CALCULATIONS), FinancialData.class));
        } catch (IOException e) {
            log.error("Could not deserialize json", e);
            throw new RuntimeException(e);
        }
        return corpusRecord;
    }

    private String scrubJson(String json) {
        return json.replace("\"value\":\"nm\"", "\"value\":0.0");
    }
}
