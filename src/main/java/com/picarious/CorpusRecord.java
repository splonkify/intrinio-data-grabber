package com.picarious;

import com.picarious.intrinio.FinancialData;
import com.picarious.intrinio.SecurityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Scope("prototype")
public class CorpusRecord {
    private static final Logger log = LoggerFactory.getLogger(CorpusRecord.class);

    public static final String MISSING = "0.0";

    private final Map<String, BigDecimal> financialsMap;
    private String classification;

    public CorpusRecord() {
        financialsMap = new HashMap<>();
    }

    public void addFinancialData(FinancialData financialData) {
        if (financialData.getData() == null) {
            return;
        }
        Map<String, BigDecimal> dataMap = financialData.toMap();
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

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public void dumpFields() {
        for (Map.Entry<String, BigDecimal> entry : financialsMap.entrySet()) {
            if (entry.getValue().doubleValue() < 10.0) {
                log.info(entry.getKey());
            }
        }

    }
}
