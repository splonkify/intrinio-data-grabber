package com.picarious;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picarious.cache.FileCache;
import com.picarious.intrinio.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        urlMap.put(FundamentalsData.class.getSimpleName(), "https://api.intrinio.com/fundamentals/standardized?ticker=%s&statement=balance_sheet&type=QTR");
        urlMap.put(PriceData.class.getSimpleName(), "https://api.intrinio.com/prices?ticker=%s&frequency=weekly&start_date=%s&end_date=%s");
        urlMap.put(FinancialData.class.getSimpleName(), "https://api.intrinio.com/financials/standardized?ticker=%s&statement=%s&fiscal_year=%s&fiscal_period=%s");
    }

    public void build(Corpus corpus) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            SecurityData securityData = findData(SecurityData.class);
//            corpusRecord.addSecurities(securityData);
            for (SecurityDatum securityDatum : securityData.getData()) {
                CorpusRecord corpusRecord = corpusRecordProvider.get();
                FundamentalsData fundamentalsData = findData(FundamentalsData.class, securityDatum.getTicker());
                if (fundamentalsData.getData() != null) {
                    PriceParameters priceParameters = new PriceParameters(fundamentalsData);
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "balance_sheet", priceParameters.getYear(), priceParameters.getQuarter()));
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "cash_flow_statement", priceParameters.getYear(), priceParameters.getQuarter()));
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "income_statement", priceParameters.getYear(), priceParameters.getQuarter()));
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "calculations", priceParameters.getYear(), priceParameters.getQuarter()));
                    PriceData quoteData = findData(PriceData.class, securityDatum.getTicker(), priceParameters.getStart(), priceParameters.getEnd());
                    corpus.addRecord(corpusRecord);
                }
            }
        } catch (IOException e) {
            log.error("Could not deserialize json", e);
            throw new RuntimeException(e);
        }
    }

    private <T> T findData(Class<T> clazz, String... parameters) throws IOException {
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
        String json = fileCache.read(fileKey);
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

    private class PriceParameters {
        private String year;
        private String quarter;
        private String start;
        private String end;

        public PriceParameters(FundamentalsData fundamentalsData) {
            this.year = "2016";
            this.quarter = "Q1";
            this.start = null;
            this.end = null;

            for (FundamentalsDatum fundamentalsDatum : fundamentalsData.getData()) {
                if (String.valueOf(fundamentalsDatum.getFiscal_year()).equals(year) && fundamentalsDatum.getFiscal_period().equals(quarter)) {
                    String filingingDateString = fundamentalsDatum.getFiling_date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date filingDate = sdf.parse(filingingDateString);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(filingDate);
                        calendar.add(Calendar.DAY_OF_YEAR, 2);
                        start = sdf.format(calendar.getTime());
                        calendar.add(Calendar.MONTH, 1);
                        end = sdf.format(calendar.getTime());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        public String getYear() {
            return year;
        }

        public String getQuarter() {
            return quarter;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
    }


}
