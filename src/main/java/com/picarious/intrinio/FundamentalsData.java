package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundamentalsData extends Paged {
    private List<FundamentalsDatum> data;

    public List<FundamentalsDatum> getData() {
        return data;
    }

    public void setData(List<FundamentalsDatum> data) {
        this.data = data;
    }

    @Override
    public <T extends Paged> void addPage(T pageEntity) {
        FundamentalsData page = (FundamentalsData) pageEntity;
        data.addAll(page.getData());
    }
}
