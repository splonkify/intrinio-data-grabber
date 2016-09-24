package com.picarious;

import com.picarious.intrinio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecentFilingFinder {

    private final Repository repository;

    @Autowired
    public RecentFilingFinder(Repository repository) {
        this.repository = repository;
    }

    public List<FundamentalsDatum> findFundamentalsOrderedByFilingDate() {
        Map<String, FundamentalsDatum> fundamentals = new HashMap<>();
        SecurityData securityData = repository.findData(false, SecurityData.class);
        for (SecurityDatum securityDatum : securityData.getData()) {
            FundamentalsData fundamentalsData = repository.findData(true, FundamentalsData.class, securityDatum.getTicker());
            if (fundamentalsData != null && fundamentalsData.getData() != null) {
                fundamentals.putAll(fundamentalsData.getData().stream()
                        .filter(f -> f.getFiscal_period().startsWith("Q"))
                        .map(f -> {f.setTicker(securityDatum.getTicker()); return f;})
                        .collect(Collectors.toMap(FundamentalsDatum::getTicker, d -> d, (d1, d2) -> d1)));
            }
        }

        // descending sort
        List<FundamentalsDatum> sortedFundamentals = new ArrayList<>(fundamentals.values());
        sortedFundamentals.sort((o1, o2) -> o2.getFiling_date().compareTo(o1.getFiling_date()));
        return sortedFundamentals;
    }
}
