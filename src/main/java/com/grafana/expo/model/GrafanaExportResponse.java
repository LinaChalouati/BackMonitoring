package com.grafana.expo.model;


public class GrafanaExportResponse {
    private String status;
    private String message;
    private byte[] dashboardJson;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getDashboardJson() {
        return dashboardJson;
    }

    public void setDashboardJson(byte[] dashboardJson) {
        this.dashboardJson = dashboardJson;
    }
}
