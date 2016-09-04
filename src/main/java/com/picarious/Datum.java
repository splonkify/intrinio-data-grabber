package com.picarious;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Created by kgiles on 9/3/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Datum {
    private String tag;
    private BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "Datum{" +
                "tag='" + tag + '\'' +
                ", value=" + value +
                '}';
    }
}
