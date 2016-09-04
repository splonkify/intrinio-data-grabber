package com.picarious;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.picarious.TagName.BASICEPS;
import static com.picarious.TagName.DELTAINCOME;
import static com.picarious.TagName.LONGTERMDEBT;

/**
 * Created by kgiles on 9/3/16.
 */
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

    public void toFile(OutputStreamWriter writer) throws IOException {
        StringJoiner stringJoiner = new StringJoiner(",", "", "\n");
        stringJoiner.add(valueFromFinancialMap(BASICEPS));
        stringJoiner.add(valueFromFinancialMap(LONGTERMDEBT));
        stringJoiner.add(valueFromFinancialMap(DELTAINCOME));

        writer.write(stringJoiner.toString());
    }

    private String valueFromFinancialMap(String key) {
        BigDecimal value = financialsMap.get(key);
        if (value != null) {
            return value.toString();
        }
        return MISSING;
    }
}
