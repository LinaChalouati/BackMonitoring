package com.grafana.expo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GrafanaExportResponse {
    private String status;
    private String message;
    private byte[] dashboardJson;


}
