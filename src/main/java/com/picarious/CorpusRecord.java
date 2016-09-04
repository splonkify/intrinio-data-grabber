package com.picarious;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Scope("prototype")
public class CorpusRecord {

    public static final String MISSING = "Missing";

    private final Map<String, BigDecimal> financialsMap;

    public CorpusRecord() {
        financialsMap = new HashMap<>();
    }

    public void addFinancialData(Data data) {
        Map<String, BigDecimal> dataMap = data.toMap();
        financialsMap.putAll(dataMap);
    }

    private String valueFromFinancialMap(String key) {
        BigDecimal value = financialsMap.get(key);
        if (value != null) {
            return value.toString();
        }
        return MISSING;
    }

    public String getValue(String key) {
        return valueFromFinancialMap(key);
    }
}
