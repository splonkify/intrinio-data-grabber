package com.picarious;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by kgiles on 9/3/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {
    private List<Datum> data;

    public Data() {
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Map<String, BigDecimal> toMap() {
        return data.stream().collect(Collectors.toMap(Datum::getTag, Datum::getValue));
    }

    public String toString() {
        return data.stream().map(tagValue -> tagValue.toString()).collect(Collectors.joining(", "));
    }

}
