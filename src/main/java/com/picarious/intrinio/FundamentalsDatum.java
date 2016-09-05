package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundamentalsDatum {
    private int fiscal_year;
    private String fiscal_period;
    private String filing_date;

    public int getFiscal_year() {
        return fiscal_year;
    }

    public void setFiscal_year(int fiscal_year) {
        this.fiscal_year = fiscal_year;
    }

    public String getFiscal_period() {
        return fiscal_period;
    }

    public void setFiscal_period(String fiscal_period) {
        this.fiscal_period = fiscal_period;
    }

    public String getFiling_date() {
        return filing_date;
    }

    public void setFiling_date(String filing_date) {
        this.filing_date = filing_date;
    }
}
