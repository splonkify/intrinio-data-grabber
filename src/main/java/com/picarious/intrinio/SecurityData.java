package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityData {
    private List<SecurityDatum> data;

    public List<SecurityDatum> getData() {
        return data;
    }

    public void setData(List<SecurityDatum> data) {
        this.data = data;
    }

}
