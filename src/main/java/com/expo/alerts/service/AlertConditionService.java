package com.expo.alerts.service;


import com.expo.alerts.model.AlertCondition;
import com.expo.alerts.model.GrafanaAlert;
import com.expo.grafana.service.GrafanaClient;
import com.expo.grafana.service.PanelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class AlertConditionService {

/*
    @Value("${grafana.apiKey}")
    private String apiKey;

    @Value("${grafana.api.url}")
    private String grafanaApiUrl;

    @Autowired
    private GrafanaClient grafanaClient;
    @Autowired
    private PanelClient panelClient;

    private HttpEntity<String> getHeaderHttp() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

  /*  public void createAlert(AlertCondition alertCondition, String dashboardTitle, String panelTitle) {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        // l url taa l creation of les alertes
        String url=grafanaApiUrl + "/api/alerts";




    }*/



}
