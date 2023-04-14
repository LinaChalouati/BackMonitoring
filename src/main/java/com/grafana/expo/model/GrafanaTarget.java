package com.grafana.expo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrafanaTarget {
    private String expr;

    public GrafanaTarget(String expr) {
        this.expr = expr;
    }

    @JsonProperty("expr")
    public String getExpr() {
        return expr;
    }

    @JsonProperty("expr")
    public void setExpr(String expr) {
        this.expr = expr;
    }
}
