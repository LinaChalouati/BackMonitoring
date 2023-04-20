package com.expo.grafana.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.stereotype.Component;

@AllArgsConstructor
@Getter
@Setter
@Component
public class GrafanaPanel {
    private String PanelTitle;
    private String PanelType;
    private String datasource;
    private String PanelId;
    public GrafanaPanel() {

    }


}
