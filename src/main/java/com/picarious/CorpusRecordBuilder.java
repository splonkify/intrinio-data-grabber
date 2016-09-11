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
    private final Boolean isApiAvailable;
    private final int targetWeeks;
    private final double threshold;
    private final String base64Creds;

    private final Map<String, String> urlMap;

    @Autowired
    public CorpusRecordBuilder(
            Provider<CorpusRecord> corpusRecordProvider,
            FileCache fileCache,
            RestTemplate restTemplate,
            @Value("${api.username}") String username,
            @Value("${api.password}") String password,
            @Value("${api.available}") Boolean isApiAvailable,
            @Value("${model.targetWeeks}") int targetWeeks,
            @Value("${model.threshhold}") double threshold
    ) {
        this.corpusRecordProvider = corpusRecordProvider;
        this.fileCache = fileCache;
        this.restTemplate = restTemplate;
        this.isApiAvailable = isApiAvailable;
        this.targetWeeks = targetWeeks;
        this.threshold = threshold;
        String plainCreds = username + ":" + password;
        base64Creds = Base64.getEncoder().encodeToString(plainCreds.getBytes());
        urlMap = new HashMap<>();
        urlMap.put(SecurityData.class.getSimpleName(), "https://api.intrinio.com/securities/search?conditions=average_daily_volume~gt~3000000");
        urlMap.put(FundamentalsData.class.getSimpleName(), "https://api.intrinio.com/fundamentals/standardized?ticker=%s&statement=balance_sheet&type=QTR");
        urlMap.put(PriceData.class.getSimpleName(), "https://api.intrinio.com/prices?ticker=%s&frequency=weekly&start_date=%s&end_date=%s");
        urlMap.put(FinancialData.class.getSimpleName(), "https://api.intrinio.com/financials/standardized?ticker=%s&statement=%s&fiscal_year=%s&fiscal_period=%s");
    }

    public void build(Corpus corpus) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            SecurityData securityData = findData(SecurityData.class);
            boolean fieldsDumped = false;
//            corpusRecord.addSecurities(securityData);
            for (SecurityDatum securityDatum : securityData.getData()) {
                CorpusRecord corpusRecord = corpusRecordProvider.get();
                FundamentalsData fundamentalsData = findData(FundamentalsData.class, securityDatum.getTicker());
                if (fundamentalsData != null && fundamentalsData.getData() != null) {
                    PriceParameters priceParameters = new PriceParameters(fundamentalsData);
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "balance_sheet", priceParameters.getYear(), priceParameters.getQuarter()));
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "cash_flow_statement", priceParameters.getYear(), priceParameters.getQuarter()));
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "income_statement", priceParameters.getYear(), priceParameters.getQuarter()));
                    corpusRecord.addFinancialData(findData(FinancialData.class, securityDatum.getTicker(), "calculations", priceParameters.getYear(), priceParameters.getQuarter()));
                    if (priceParameters.getStart() != null && priceParameters.getEnd() != null) {
                        PriceData priceData = findData(PriceData.class, securityDatum.getTicker(), priceParameters.getStart(), priceParameters.getEnd());
                        if (priceData != null && priceData.getData() != null && !priceData.getData().isEmpty()) {
                            String classification = classify(priceData);
                            corpusRecord.setClassification(classification);
                            corpus.addRecord(corpusRecord);
                            if (!fieldsDumped) {
//                            corpusRecord.dumpFields();
                                fieldsDumped = true;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Could not deserialize json", e);
            throw new RuntimeException(e);
        }
    }

    private String classify(PriceData priceData) {
        String classification = "Z";
        priceData.getData().sort(new Comparator<PriceDatum>() {
            @Override
            public int compare(PriceDatum o1, PriceDatum o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        double firstDayOpen = priceData.getData().get(0).getOpen().doubleValue();
        double lastDayHigh = priceData.getData().get(priceData.getData().size() - 1).getHigh().doubleValue();
        double changeRate = (lastDayHigh - firstDayOpen) / firstDayOpen;
        if (Math.abs(changeRate) > threshold) {
            if (changeRate > 0.0) {
                classification = "P";
            } else {
                classification = "N";
            }
        }
        return classification;
    }

    private <T extends Paged> T findData(Class<T> clazz, String... parameters) throws IOException {
        int current_page = 1;
        T entity = null;
        do {
            String urlKey = clazz.getSimpleName();
            String fileKey = urlKey;
            if (current_page > 1) {
                fileKey = fileKey + "_p" + current_page;
            }
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
                json = getPagedDataFromServer(clazz, urlKey, current_page, parameters);
                fileCache.write(fileKey, json);
            }
            ObjectMapper mapper = new ObjectMapper();
            T pageEntity = mapper.readValue(scrubJson(json), clazz);
            if (current_page == 1) {
                entity = pageEntity;
            } else {
                entity.addPage(pageEntity);
            }
            current_page++;
        } while (entity != null && entity.getCurrent_page() < entity.getTotal_pages());
        return entity;
    }

    private <T extends Paged> String getPagedDataFromServer(Class<T> clazz, String urlKey, int page, String... parameters) throws IOException {
        if (!isApiAvailable) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = urlMap.get(urlKey);
        if (parameters.length > 0) {
            url = String.format(url, parameters);
        }
        if (page > 1) {
            url = url + "&page_number=" + page;
        }
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        String json = response.getBody();

        return json;
    }

    private String scrubJson(String json) {
        String scrubbed = json.replace("\"value\":\"nm\"", "\"value\":0.0");
        scrubbed = scrubbed.replace("\"value\":NaN", "\"value\":1.0");
        return scrubbed;
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
                        calendar.add(Calendar.WEEK_OF_YEAR, targetWeeks);
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
