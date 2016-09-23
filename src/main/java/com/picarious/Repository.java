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

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class Repository {
    private static final Logger log = LoggerFactory.getLogger(Repository.class);

    private final FileCache fileCache;
    private final RestTemplate restTemplate;
    private final Boolean isApiAvailable;
    private final String base64Creds;

    private final Map<String, String> urlMap;

    @Autowired
    public Repository(
            FileCache fileCache,
            RestTemplate restTemplate,
            @Value("${api.username}") String username,
            @Value("${api.password}") String password,
            @Value("${api.available}") Boolean isApiAvailable
            ) {
        this.fileCache = fileCache;
        this.restTemplate = restTemplate;
        this.isApiAvailable = isApiAvailable;
        String plainCreds = username + ":" + password;
        base64Creds = Base64.getEncoder().encodeToString(plainCreds.getBytes());
        urlMap = new HashMap<>();
        urlMap.put(SecurityData.class.getSimpleName(), "https://api.intrinio.com/securities/search?conditions=average_daily_volume~gt~3000000");
        urlMap.put(FundamentalsData.class.getSimpleName(), "https://api.intrinio.com/fundamentals/standardized?ticker=%s&statement=balance_sheet&type=QTR");
        urlMap.put(PriceData.class.getSimpleName(), "https://api.intrinio.com/prices?ticker=%s&frequency=weekly&start_date=%s&end_date=%s");
        urlMap.put(FinancialData.class.getSimpleName(), "https://api.intrinio.com/financials/standardized?ticker=%s&statement=%s&fiscal_year=%s&fiscal_period=%s");
    }

    public <T extends Paged> T findData(Class<T> clazz, String... parameters) {
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
                if (!isApiAvailable) {
                    return null;
                }
                json = getPagedDataFromServer(clazz, urlKey, current_page, parameters);
                fileCache.write(fileKey, json);
            }
            ObjectMapper mapper = new ObjectMapper();
            T pageEntity = null;
            try {
                pageEntity = mapper.readValue(scrubJson(json), clazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (current_page == 1) {
                entity = pageEntity;
            } else {
                entity.addPage(pageEntity);
            }
            current_page++;
        } while (entity != null && entity.getCurrent_page() < entity.getTotal_pages());
        return entity;
    }

    private <T extends Paged> String getPagedDataFromServer(Class<T> clazz, String urlKey, int page, String... parameters) {
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

}
