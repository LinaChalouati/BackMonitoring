package com.grafana.expo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class GrafanaClient {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;

    private HttpEntity<String> getHeaderHttp(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        return requestEntity;

    }

    public void createDashboard(String jsonPayload) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboard = objectMapper.readValue(jsonPayload, ObjectNode.class);

        ObjectNode requestPayload = objectMapper.createObjectNode();
        requestPayload.set("dashboard", dashboard);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestPayload.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, requestEntity, String.class);
    }

  public String getDashboardIdByTitle(String dashboardTitle) throws IOException {
      HttpEntity<String> requestEntity=this.getHeaderHttp();
      RestTemplate restTemplate = new RestTemplate();
      System.out.println(restTemplate);
      ResponseEntity<String> response = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
      //System.out.println("l response"+response);

      JsonNode root = new ObjectMapper().readTree(response.getBody());
      //System.out.println(root);

      JsonNode firstResult = root.get(0);
      //System.out.println(firstResult);

      String dashboardId = firstResult.get("id").asText();
      //System.out.println("id"+dashboardId);

      return dashboardId;
  }


    public void addPanel(String dashboardTitle, String targetExpr,String PanelTitle) throws JsonProcessingException {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        System.out.println("AHHHHHH"+searchResultNode);
        String dashboardId = searchResultNode.get(0).get("uid").asText();
         System.out.println("lid"+dashboardId);
        //njib l json taa l dashboard
       // dashboardId="FkFZ5nPVz";
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();
         System.out.println("dashboardJson"+dashboardJson);

        // Update the dashboard JSON with the new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
         System.out.println("dashboardNode"+dashboardNode);

        // Create l new panel
        ObjectNode panelNode = objectMapper.createObjectNode();
        panelNode.put("title", PanelTitle);
        panelNode.put("type", "graph");
        panelNode.put("datasource", "Prometheus");

        ArrayNode targetsNode = objectMapper.createArrayNode();
        ObjectNode targetNode = objectMapper.createObjectNode();
        targetNode.put("expr", targetExpr);
        targetsNode.add(targetNode);

        panelNode.set("targets", targetsNode);

        System.out.println("panelNode"+panelNode);

        //System.out.println( dashboardNode.path("dashboard").path("panels"));
        // l hne win l mochkla
        System.out.println("l final"+dashboardNode.path("dashboard").path("panels"));

        ArrayNode panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        System.out.println("panelsNode"+panelsNode);

        panelsNode.add(panelNode);
        System.out.println("MRIGL"+panelsNode);



        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        System.out.println("lupdate"+updateRequestEntity);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }
    public void deletePanel(String dashboardTitle, String panelTitle) throws JsonProcessingException {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Get the dashboard JSON and remove the panel
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        boolean panelDeleted = false;
        for (int i = 0; i < panelsNode.size(); i++) {
            JsonNode panelNode = panelsNode.get(i);
            if (panelNode.get("title").asText().equals(panelTitle)) {
                panelsNode.remove(i);
                panelDeleted = true;
                break;
            }
        }
        if (!panelDeleted) {
            throw new RuntimeException("Panel not found");
        }

        // Update the dashboard in Grafana
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }

    public void updatePanel(String dashboardTitle, String panelTitle, ObjectNode updatedPanel) throws JsonProcessingException {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Fetch the dashboard JSON from Grafana
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // Update the dashboard JSON with the new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        for (JsonNode panelNode : panelsNode) {
            if (panelNode.path("title").asText().equals(panelTitle)) {
                ((ObjectNode) panelNode).setAll(updatedPanel);
                break;
            }
        }

        // Send the updated dashboard JSON to Grafana
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }

    public void deleteDashboard(String dashboardTitle) {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode;
        try {
            searchResultNode = new ObjectMapper().readTree(searchResultJson);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing search result JSON: " + e.getMessage(), e);
        }
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Delete the dashboard
        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> deleteRequestEntity = new HttpEntity<>(deleteHeaders);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.DELETE, deleteRequestEntity, String.class);

        // Check delete was mrigl wale
        if (deleteResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard deleted successfully");
        } else {
            throw new RuntimeException("Dashboard delete failed: " + deleteResponse.getStatusCodeValue() + " " + deleteResponse.getBody());
        }
    }


}

