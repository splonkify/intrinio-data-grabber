package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceData {
    private List<PriceDatum> data;

    public List<PriceDatum> getData() {
        return data;
    }

    public void setData(List<PriceDatum> data) {
        this.data = data;
    }
}
