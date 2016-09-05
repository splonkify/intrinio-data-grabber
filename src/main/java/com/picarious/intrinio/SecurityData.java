package com.picarious.intrinio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
