package com.grafana.expo.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class DashboardManager {
    @Value("${grafana.apiKey}")
    private String apiKey;
    @Value("${grafana.url}")

    private String grafanaUrl;

    public DashboardManager(String apiKey, String grafanaUrl) {
        this.apiKey = apiKey;
        this.grafanaUrl = grafanaUrl;
    }




}
