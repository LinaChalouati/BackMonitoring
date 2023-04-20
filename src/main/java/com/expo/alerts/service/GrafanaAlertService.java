package com.expo.alerts.service;

import com.expo.alerts.model.GrafanaAlert;
import com.expo.grafana.model.GrafanaDashboard;
import com.expo.grafana.model.GrafanaPanel;
import com.expo.grafana.service.GrafanaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GrafanaAlertService {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GrafanaClient grafanaClient;


    public GrafanaAlertService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, GrafanaClient grafanaClient) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.grafanaClient = grafanaClient;
    }

    public void createPanelAlert(GrafanaAlert alert, String dashboardTitle) throws JsonProcessingException {
        List<JsonNode> panels = grafanaClient.GetPanels(dashboardTitle);
        if (panels.size() > 0) {
         //   GrafanaPanel panel = panels.get(0);
            JsonNode panel=panels.get(0);
            String url = grafanaUrl + "/api/alerts";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
           // alert.setDashboardId(panel.getDashboardId());
           // alert.setPanelId(panel.getId());
            String alertJson = objectMapper.writeValueAsString(alert);
            HttpEntity<String> entity = new HttpEntity<>(alertJson, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        }
    }

    public void createDashboardAlert(GrafanaAlert alert) throws JsonProcessingException {
        GrafanaDashboard dashboard = grafanaClient.getDashboard(alert.getDashboardId());
        if (dashboard != null) {
            String url = grafanaUrl + "/api/alerts";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String alertJson = objectMapper.writeValueAsString(alert);
            HttpEntity<String> entity = new HttpEntity<>(alertJson, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        }
    }

    public void updatePanelAlert(GrafanaAlert alert) throws JsonProcessingException {
        String url = grafanaUrl + "/api/alerts/" + alert.getPanelId() + "/conditions/" + alert.getAlertName();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String alertJson = objectMapper.writeValueAsString(alert);
        HttpEntity<String> entity = new HttpEntity<>(alertJson, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    public void updateDashboardAlert(GrafanaAlert alert) throws JsonProcessingException {
        String url = grafanaUrl + "/api/alerts/" + alert.getDashboardId() + "/conditions/" + alert.getAlertName();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String alertJson = objectMapper.writeValueAsString(alert);
        HttpEntity<String> entity = new HttpEntity<>(alertJson, headers);
        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

}
