package com.expo.alerts.service;

import com.expo.alerts.model.AlertCondition;
import com.expo.grafana.model.GrafanaPanel;
import com.expo.grafana.service.GrafanaClient;
import com.expo.grafana.service.PanelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


//l partie hedhi bch tetbadel , à savoir , instead i'll be using prometheus for the alertes
@Service
public class GrafanaAlertService {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GrafanaClient grafanaClient;
    private final PanelClient grafanapanel;
    private final AlertCondition alertCondition;





    public GrafanaAlertService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, GrafanaClient grafanaClient, GrafanaPanel grafanapanel, PanelClient grafanapanel1, AlertCondition alertCondition) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.grafanaClient = grafanaClient;
        this.grafanapanel = grafanapanel1;
        this.alertCondition = alertCondition;
    }
/*
    public void createPanelAlert(GrafanaAlert alert,String panelTitle,String dashboardTitle,AlertCondition alertCondition) throws IOException {

        JsonNode panel=grafanapanel.getPanelByTitle(panelTitle,dashboardTitle);
        String panelId=grafanapanel.getPanelIdByTitle(dashboardTitle,panelTitle);
        String dashboardId=grafanaClient.getDashboardIdByTitle(dashboardTitle);
        String url = grafanaUrl + "/api/alerts";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        alert.setDashboardId(dashboardId);
        alert.setPanelId(panelId);
            String alertJson = objectMapper.writeValueAsString(alert);
            HttpEntity<String> entity = new HttpEntity<>(alertJson, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    }
    public void setPanelAlert(String dashboardTitle, String panelTitle, String alertName, String conditionType, String conditionOperator, double threshold) throws IOException {
        // Get the panel by title
        JsonNode panel = grafanapanel.getPanelByTitle(dashboardTitle, panelTitle);
        if (panel != null) {
            // Construct the alert object
            GrafanaAlert alert = new GrafanaAlert();
            alert.setAlertName(alertName);
            String panelId=grafanapanel.getPanelIdByTitle(dashboardTitle,panelTitle);
            String dashboardId=grafanaClient.getDashboardIdByTitle(dashboardTitle);
            alert.setDashboardId(dashboardId);
            alert.setPanelId(panelId);

            // Construct the condition object
            AlertCondition condition = new AlertCondition();
            condition.setType(conditionType);
            condition.setOperator(conditionOperator);
            condition.setThreshold(String.valueOf(threshold));
            List<GrafanaCondition> conditions = new ArrayList<>();
            conditions.add(condition);

            // Set the condition object in the alert
            alert.setConditions(conditions);

            // Send the alert to Grafana API
            String url = grafanaUrl + "/api/alerts";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String alertJson = objectMapper.writeValueAsString(alert);
            HttpEntity<String> entity = new HttpEntity<>(alertJson, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        }
    }*/

/*
    public void createDashboardAlert(GrafanaAlert alert) throws JsonProcessingException {
        JsonNode dashboard = grafanaClient.getDashboard(alert.getDashboardId());
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
    }*/

}
