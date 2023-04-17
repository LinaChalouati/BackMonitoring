package com.grafana.expo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
public class GrafanaClient {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;
    public String GetDashboard(String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        System.out.print(requestEntity);
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);

        if (searchResponse.getStatusCodeValue() != 200) {
            throw new RuntimeException("Failed to retrieve dashboard search result: " + searchResponse.getBody());
        }
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        if (dashboardResponse.getStatusCodeValue() != 200) {
            throw new RuntimeException("Failed to retrieve dashboard: " + dashboardResponse.getBody());
        }
        String dashboardJson = dashboardResponse.getBody();
        return dashboardJson;
    }
  /*  public String findDashboardById(String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        System.out.print(requestEntity);
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);

        if (searchResponse.getStatusCodeValue() != 200) {
            throw new RuntimeException("Failed to retrieve dashboard search result: " + searchResponse.getBody());
        }
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        if (dashboardResponse.getStatusCodeValue() != 200) {
            throw new RuntimeException("Failed to retrieve dashboard: " + dashboardResponse.getBody());
        }
        String dashboardJson = dashboardResponse.getBody();
        return dashboardJson;
    }*/

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
    public void modifyDashboard(String dashboardTitle, String newTitle, String newDescription, List<String> tags) throws JsonProcessingException {
        // Get the existing dashboard
        String dashboardJson = this.GetDashboard(dashboardTitle);

        // Parse the JSON into an object
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);

        // Modify the title, description, and tags
        dashboardNode.put("title", newTitle);
        dashboardNode.put("description", newDescription);
        ArrayNode tagsNode = dashboardNode.putArray("tags");
        for (String tag : tags) {
            tagsNode.add(tag);
        }

        // Convert the modified object back to JSON
        String modifiedDashboardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);

        // Update the dashboard in Grafana
        updateDashboard(modifiedDashboardJson);
    }

    public void updateDashboard(String dashboardJson) throws JsonProcessingException {
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        String url = grafanaUrl + "api/dashboards/db";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class, dashboardJson);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to update dashboard: " + response.getBody());
        }
    }


    public String findDashboard(String query) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        // Search for dashboards
        ResponseEntity<String> dashboardSearchResponse = restTemplate.exchange(grafanaUrl + "api/search?type=dash-db&query=" + query, HttpMethod.GET, requestEntity, String.class);
        System.out.println("Dashboard search status code: " + dashboardSearchResponse.getStatusCode());
        System.out.println("Dashboard search response body: " + dashboardSearchResponse.getBody());



        // Return the combined search results
        return dashboardSearchResponse.getBody();
    }

}

