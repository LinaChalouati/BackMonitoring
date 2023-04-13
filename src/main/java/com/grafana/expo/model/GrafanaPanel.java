package com.grafana.expo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GrafanaPanel {
    private String PanelType;
    private String PanelId;
    private String PanelTitle;
    private String PanelDatasource;
    private String PanelTargets;

    public GrafanaPanel() {
    }
}
