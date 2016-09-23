package com.picarious;

import com.picarious.intrinio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

@Service
public class CorpusRecordBuilder {
    private static final Logger log = LoggerFactory.getLogger(CorpusRecordBuilder.class);

    private final Provider<CorpusRecord> corpusRecordProvider;
    private final Repository repository;
    private final int targetWeeks;
    private final double threshold;

    @Autowired
    public CorpusRecordBuilder(
            Provider<CorpusRecord> corpusRecordProvider,
            Repository repository,
            @Value("${model.targetWeeks}") int targetWeeks,
            @Value("${model.threshhold}") double threshold
    ) {
        this.corpusRecordProvider = corpusRecordProvider;
        this.repository = repository;
        this.targetWeeks = targetWeeks;
        this.threshold = threshold;
    }

    public void build(Corpus corpus) {
        SecurityData securityData = repository.findData(SecurityData.class);
        boolean fieldsDumped = false;
        for (SecurityDatum securityDatum : securityData.getData()) {
            CorpusRecord corpusRecord = corpusRecordProvider.get();
            FundamentalsData fundamentalsData = repository.findData(FundamentalsData.class, securityDatum.getTicker());
            if (fundamentalsData != null && fundamentalsData.getData() != null) {
                PriceParameters priceParameters = new PriceParameters(fundamentalsData);
                corpusRecord.addFinancialData(repository.findData(FinancialData.class, securityDatum.getTicker(), "balance_sheet", priceParameters.getYear(), priceParameters.getQuarter()));
                corpusRecord.addFinancialData(repository.findData(FinancialData.class, securityDatum.getTicker(), "cash_flow_statement", priceParameters.getYear(), priceParameters.getQuarter()));
                corpusRecord.addFinancialData(repository.findData(FinancialData.class, securityDatum.getTicker(), "income_statement", priceParameters.getYear(), priceParameters.getQuarter()));
                corpusRecord.addFinancialData(repository.findData(FinancialData.class, securityDatum.getTicker(), "calculations", priceParameters.getYear(), priceParameters.getQuarter()));
                if (priceParameters.getStart() != null && priceParameters.getEnd() != null) {
                    PriceData priceData = repository.findData(PriceData.class, securityDatum.getTicker(), priceParameters.getStart(), priceParameters.getEnd());
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
    }

    private String classify(PriceData priceData) {
        String classification = "Z";
        priceData.getData().sort((o1, o2) -> o1.getDate().compareTo(o2.getDate()));
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

    private class PriceParameters {
        private String year;
        private String quarter;
        private String start;
        private String end;

        public PriceParameters(FundamentalsData fundamentalsData) {
            this.year = "2016";
            this.quarter = "Q2";
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
