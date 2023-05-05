package com.expo.grafana.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PanelClient {
    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;
    @Autowired
    private GrafanaClient grafanaClient;

  /*  public List<JsonNode> GetPanelsNames(String dashboardTitle) throws JsonProcessingException {
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dashboardNode = objectMapper.readTree(dashboardJson);
        List<JsonNode> panels = new ArrayList<>();
        for (JsonNode panelNode : dashboardNode.get("dashboard").get("panels")) {
            panels.add(panelNode);
        }
        System.out.println("OKKKK");
        System.out.println(panels);

        return panels;
    }
*/

    private HttpEntity<String> getHeaderHttp() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    public void addPanel(String dashboardTitle,String PanelTitle,String targetExpr) throws JsonProcessingException {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        // System.out.println(PanelTitle);
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
        System.out.println("panelsNode"+panelsNode);
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

    public void modifyPanel(String dashboardTitle, int panelId, String newTitle, String newType) throws Exception {
        // First, retrieve the existing dashboard JSON
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);

        // Convert the JSON string to a JSON object using Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dashboardNode = mapper.readTree(dashboardJson).get("dashboard");
      //  System.out.println(dashboardNode.get("dashboard").get("panels"));
        // Find the panel to modify based on the panel ID
        JsonNode panelNode = null;
        for (JsonNode panel : dashboardNode.get("panels")) {
            if (panel.get("id").asInt() == panelId) {
                panelNode = panel;
                break;
            }
        }

        if (panelNode == null) {
            throw new Exception("Panel with ID " + panelId + " not found in dashboard " + dashboardTitle);
        }

        if (newTitle != null && !newTitle.isEmpty()) {
            ((ObjectNode) panelNode).put("title", newTitle);
        }

        if (newType != null && !newType.isEmpty()) {
            ((ObjectNode) panelNode).put("type", newType);
        }



        String modifiedDashboardJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);

        grafanaClient.updateDashboard(modifiedDashboardJson);

    }

    public JsonNode getPanelByTitle(String dashboardTitle, String panelTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        // Search for the dashboard
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found with title: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Get the dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // Find the panel with the given title in the dashboard JSON
        JsonNode dashboardNode = new ObjectMapper().readTree(dashboardJson);
        JsonNode panelsNode = dashboardNode.get("dashboard").get("panels");
        for (JsonNode panelNode : panelsNode) {
            if (panelNode.get("title").asText().equals(panelTitle)) {
                return panelNode;
            }
        }

        throw new RuntimeException("Panel not found with title: " + panelTitle);
    }


    //normalement mrigl
    public JsonNode getPanelById(String panelId,String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        //  Wini l dashboard
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found with title: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // jib l dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // wini l panel fl dashboard JSON
        JsonNode dashboardNode = new ObjectMapper().readTree(dashboardJson);
        JsonNode panelsNode = dashboardNode.get("dashboard").get("panels");
        for (JsonNode panelNode : panelsNode) {
            if (panelNode.get("id").asText().equals(panelId)) {
             //   System.out.println(panelNode.toString());
                return panelNode;
            }
        }

        throw new RuntimeException("Panel not found with title: " + panelId);
    }
    public String getPanelIdByTitle(String dashboardTitle, String panelTitle) throws IOException {
        // Get headers and set up RestTemplate
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        // Get dashboard ID
        String dashboardUid = grafanaClient.getDashboardUidByTitle(dashboardTitle);

        // Search for panels in the dashboard
        ResponseEntity<String> panelSearchResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardUid, HttpMethod.GET, requestEntity, String.class);
        JsonNode dashboardJson = new ObjectMapper().readTree(panelSearchResponse.getBody());
        JsonNode panels = dashboardJson.at("/dashboard/panels");
        //System.out.println(dashboardJson);

        // Iterate through the panels to find the one with matching title
        for (JsonNode panel : panels) {
            if (panel.get("title").asText().equals(panelTitle)) {
                System.out.println(panel.get("id").asText());
                return panel.get("id").asText();
            }
        }

        // If panel with matching title not found, throw exception
        throw new RuntimeException("Panel not found: " + panelTitle);
    }



}
