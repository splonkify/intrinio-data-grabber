package com.picarious;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by kgiles on 9/4/16.
 */
@Service
public class StandardizedFinancials {
    private static final Logger log = LoggerFactory.getLogger(StandardizedFinancials.class);

    public void statements(CorpusRecord corpusRecord) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_BALANCE_SHEET), Data.class));
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_CASH_FLOW_STATEMENT), Data.class));
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_INCOME_STATEMENT), Data.class));
            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_CALCULATIONS), Data.class));
        } catch (IOException e) {
            log.error("Could not deserialize json", e);
            return;
        }
        log.info(corpusRecord.toString());

    }

    private String scrubJson(String json) {
        return json.replace("\"value\":\"nm\"", "\"value\":0.0");
    }
}
