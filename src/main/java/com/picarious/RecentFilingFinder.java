package com.picarious;

import com.picarious.intrinio.FundamentalsData;
import com.picarious.intrinio.FundamentalsDatum;
import com.picarious.intrinio.SecurityData;
import com.picarious.intrinio.SecurityDatum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecentFilingFinder {

    private final Repository repository;

    @Autowired
    public RecentFilingFinder(Repository repository) {
        this.repository = repository;
    }

    public List<FundamentalsDatum> findFundamentalsOrderedByFilingDate() {
        List<FundamentalsDatum> fundamentals = new ArrayList<>();
        SecurityData securityData = repository.findData(SecurityData.class);
        for (SecurityDatum securityDatum : securityData.getData()) {
            FundamentalsData fundamentalsData = repository.findData(FundamentalsData.class, securityDatum.getTicker());
            if (fundamentalsData != null && fundamentalsData.getData() != null) {
                fundamentals.addAll(fundamentalsData.getData().stream().map(f -> {f.setTicker(securityDatum.getTicker()); return f;}).collect(Collectors.toList()));
            }
        }

        // descending sort
        fundamentals.sort((o1, o2) -> o2.getFiling_date().compareTo(o1.getFiling_date()));
        return fundamentals;
    }
}
