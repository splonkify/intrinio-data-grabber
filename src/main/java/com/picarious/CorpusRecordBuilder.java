package com.picarious;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picarious.cache.FileCache;
import com.picarious.intrinio.FinancialData;
import com.picarious.intrinio.JsonTestData;
import com.picarious.intrinio.SecurityData;
import com.picarious.intrinio.SecurityDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class CorpusRecordBuilder {
    private static final Logger log = LoggerFactory.getLogger(CorpusRecordBuilder.class);

    private final Provider<CorpusRecord> corpusRecordProvider;
    private final FileCache fileCache;
    private final RestTemplate restTemplate;
    private final String base64Creds;

    private final Map<String, String> urlMap;

    @Autowired
    public CorpusRecordBuilder(
            Provider<CorpusRecord> corpusRecordProvider,
            FileCache fileCache,
            RestTemplate restTemplate,
            @Value("${api.username}") String username,
            @Value("${api.password}") String password
    ) {
        this.corpusRecordProvider = corpusRecordProvider;
        this.fileCache = fileCache;
        this.restTemplate = restTemplate;
        String plainCreds = username + ":" + password;
        base64Creds = Base64.getEncoder().encodeToString(plainCreds.getBytes());
        urlMap = new HashMap<>();
        urlMap.put(SecurityData.class.getSimpleName(), "https://api.intrinio.com/securities/search?conditions=average_daily_volume~gt~10000000");
        urlMap.put(FinancialData.class.getSimpleName(), "https://api.intrinio.com/financials/standardized?ticker=%s&statement=%s&fiscal_year=%s&fiscal_period=%s");
    }

    public void build(Corpus corpus) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            SecurityData securityData = findData(SecurityData.class);
//            corpusRecord.addSecurities(securityData);
            for (SecurityDatum securityDatum : securityData.getData()) {
                CorpusRecord corpusRecord = corpusRecordProvider.get();
                corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "balance_sheet", "2016", "Q1"));
                corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "cash_flow_statement", "2016", "Q1"));
                corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "income_statement", "2016", "Q1"));
                corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "calculations", "2016", "Q1"));
                corpus.addRecord(corpusRecord);
            }
//            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_BALANCE_SHEET), FinancialData.class));
//            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_CASH_FLOW_STATEMENT), FinancialData.class));
//            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_INCOME_STATEMENT), FinancialData.class));
//            corpusRecord.addFinancialData(mapper.readValue(scrubJson(JsonTestData.EBAY_CALCULATIONS), FinancialData.class));
        } catch (IOException e) {
            log.error("Could not deserialize json", e);
            throw new RuntimeException(e);
        }
//        return corpusRecord;
    }

    private <T> T findData(Class<T> clazz, String ... parameters) throws IOException {
        String urlKey = clazz.getSimpleName();
        String fileKey = urlKey;
        if (parameters.length > 0) {
            StringJoiner sj = new StringJoiner("_", "", "");
            sj.add(urlKey);
            for (String param : parameters) {
                sj.add(param);
            }
            fileKey = sj.toString();
        }
        String json = fileCache.read(urlKey);
        if (json == null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Basic " + base64Creds);
            HttpEntity<String> request = new HttpEntity<>(headers);
            String url = urlMap.get(urlKey);
            if (parameters.length > 0) {
                url = String.format(url, parameters);
            }
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            json = response.getBody();
            fileCache.write(fileKey, json);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(scrubJson(json), clazz);
    }

    private String scrubJson(String json) {
        return json.replace("\"value\":\"nm\"", "\"value\":0.0");
    }
}
