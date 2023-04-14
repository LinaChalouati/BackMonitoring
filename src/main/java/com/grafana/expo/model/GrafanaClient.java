package com.grafana.expo.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
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
  /*public void addPanel(String jsonPayLoad) throws JsonProcessingException{
    ObjectMapper objectMapper= new ObjectMapper();
    ObjectNode panel=objectMapper.readValue(jsonPayLoad,ObjectNode.class);
     ObjectNode requestPayload=objectMapper.createObjectNode();
     requestPayload.set("panel",panel);
     HttpHeaders headers= new HttpHeaders();
     headers.setContentType(MediaType.APPLICATION_JSON);
     headers.set("Authorization", "Bearer " + apiKey);
    HttpEntity<String> requestEnt

}*/
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


    public void addPanel(String dashboardTitle, String targetExpr) throws JsonProcessingException {
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
       // System.out.println(dashboardId);
        // Fetch the dashboard JSON from Grafana
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();
      //  System.out.println("dashboardJson"+dashboardJson);

        // Update the dashboard JSON with the new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
       // System.out.println("dashboardNode"+dashboardNode);

        // Create the new panel
        ObjectNode panelNode = objectMapper.createObjectNode();
        panelNode.put("title", "New Panel");
        panelNode.put("type", "graph");
        panelNode.put("datasource", "Prometheus");

        ArrayNode targetsNode = objectMapper.createArrayNode();
        ObjectNode targetNode = objectMapper.createObjectNode();
        targetNode.put("expr", targetExpr);
        targetsNode.add(targetNode);

        panelNode.set("targets", targetsNode);
     //   System.out.println("panelNode"+panelNode);


        // Add the new panel to the dashboard
        ArrayNode panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
       // System.out.println("panelsNode"+panelsNode);

        panelsNode.add(panelNode);
        System.out.println("MRIGL"+panelsNode);


        // Update the dashboard on Grafana
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, updateRequestEntity, String.class);

        System.out.println("T3addaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        System.out.println("welyeeeeeeeeeeeeeeeeeeeeeeeeeey");
        System.out.println(dashboardResponse);

    }



}

