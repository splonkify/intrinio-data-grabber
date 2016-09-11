package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FinancialData extends Paged {
    private List<FinancialDatum> data;

    public FinancialData() {
    }

    public List<FinancialDatum> getData() {
        return data;
    }

    public void setData(List<FinancialDatum> data) {
        this.data = data;
    }

    public Map<String, BigDecimal> toMap() {
        return data.stream().collect(Collectors.toMap(FinancialDatum::getTag, FinancialDatum::getValue));
    }

    public String toString() {
        return data.stream().map(tagValue -> tagValue.toString()).collect(Collectors.joining(", "));
    }

    @Override
    public <T extends Paged> void addPage(T pageEntity) {
        FinancialData page = (FinancialData) pageEntity;
        data.addAll(page.getData());
    }
}
