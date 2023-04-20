package com.expo.grafana.model;



public class GrafanaExportRequest {
    private String dashboardUid;
    private String panelId;

    public  GrafanaExportRequest(){}

    public GrafanaExportRequest(String dashboardUid, String panelId) {
        this.dashboardUid = dashboardUid;
        this.panelId = panelId;

    }

    public String getDashboardUid() {
        return dashboardUid;
    }

    public void setDashboardUid(String dashboardUid) {
        this.dashboardUid = dashboardUid;
    }

    public String getPanelId() {
        return panelId;
    }

    public void setPanelId(String panelId) {
        this.panelId = panelId;
    }


}
