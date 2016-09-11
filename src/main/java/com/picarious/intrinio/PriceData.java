package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceData extends Paged {
    private List<PriceDatum> data;

    public List<PriceDatum> getData() {
        return data;
    }

    public void setData(List<PriceDatum> data) {
        this.data = data;
    }

    @Override
    public <T extends Paged> void addPage(T pageEntity) {
        PriceData page = (PriceData) pageEntity;
        data.addAll(page.getData());
    }
}
