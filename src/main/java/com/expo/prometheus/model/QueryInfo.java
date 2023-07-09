package com.expo.prometheus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class QueryInfo {
    private Map<String, String> queryMap;

    // Constructor to initialize the queryMap field
    public QueryInfo() {
        this.queryMap = new HashMap<>();
    }
}
