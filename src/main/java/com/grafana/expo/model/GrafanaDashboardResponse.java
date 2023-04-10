package com.grafana.expo.model;

import java.util.Map;


public class GrafanaDashboardResponse {
    private String dashboardUid;
    private String dashboardJson;

    public GrafanaDashboardResponse() {}

    public String getDashboardUid() {
        return dashboardUid;
    }

    public void setDashboardUid(String dashboardUid) {
        this.dashboardUid = dashboardUid;
    }

    public String getDashboardJson() {
        return dashboardJson;
    }

    public void setDashboardJson(String dashboardJson) {
        this.dashboardJson = dashboardJson;
    }
}


