package com.expo.alerts.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GrafanaAlert {
    private String title;
    private Integer dashboardId;
    private Integer panelId;
    private String query;
    private AlertCondition condition;

    public GrafanaAlert() {}

    public GrafanaAlert(String title, Integer dashboardId, Integer panelId, String query, AlertCondition condition) {
        this.title = title;
        this.dashboardId = dashboardId;
        this.panelId = panelId;
        this.query = query;
        this.condition = condition;
    }

}
