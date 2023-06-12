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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//kiff kiff à verifier le path du panels here
// f actia temchili .get("dashboard").get(panels) f pc mteei .get("rows").get(0).get("panels") c donc à verifier aalech (tested 06 Mai 2023)
//ps il y'avait un changement de la version du grafana que j'utilise donc peut etre l json à générer tbadlet l format mteeo
@CrossOrigin(origins = "*")


@Service
public class PanelClient {
    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;
    @Autowired
    private GrafanaClient grafanaClient;


    private HttpEntity<String> getHeaderHttp() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    public void addPanel(String dashboardTitle, String panelTitle, String targetExpr, String chart,Integer id,String tag) throws JsonProcessingException {
        JsonNode dashboardPanelNode;
        // Searching for the dashboard
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Retrieve the current dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId+"?overwrite=true", HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // Update the dashboard JSON with the new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        System.out.println("l mochkla lena");
        // Check if the dashboard structure is valid

        System.out.println("dashboardNode"+dashboardNode);
        if (dashboardNode.path("dashboard").has("rows")) {
            System.out.println("d5al lena"+dashboardNode.path("dashboard").path("rows"));

            ArrayNode rowsNode = (ArrayNode) dashboardNode.path("dashboard").path("rows");
            ArrayNode panelsNode = (ArrayNode) rowsNode.get(0).path("panels");
            dashboardPanelNode = panelsNode;
        } else {
            System.out.println("d5al lena2"+dashboardNode.path("dashboard").path("panels"));

            dashboardPanelNode = dashboardNode.path("dashboard").path("panels");

        }



        // Create a new panel
        ObjectNode panelNode = objectMapper.createObjectNode();
        panelNode.put("title", panelTitle);
        panelNode.put("id", id);

        panelNode.put("type", chart);
        panelNode.put("datasource", "Prometheus");

        ArrayNode targetsNode = objectMapper.createArrayNode();
        ObjectNode targetNode = objectMapper.createObjectNode();
        targetNode.put("expr", targetExpr);
        targetNode.put("tags", tag);
        targetsNode.add(targetNode);

        panelNode.set("targets", targetsNode);

        // Add the panel to the panels array
        ((ArrayNode) dashboardPanelNode).add(panelNode);
        dashboardNode.put("overwrite", true);


        // Send the updated dashboard JSON to the server
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        System.out.println(dashboardNode);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db", HttpMethod.POST, updateRequestEntity, String.class);

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
        ArrayNode panelsNode;
        //updated
        if (dashboardNode.path("dashboard").has("rows")) {
            panelsNode =(ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
            System.out.println("panelsnode1"+panelsNode);
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
            System.out.println("panelsnode2"+panelsNode);
        }


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
    public void deletePanelById(String dashboardTitle, String panelId) throws JsonProcessingException {
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
        System.out.println("dashboardNode"+dashboardNode);
        ArrayNode panelsNode;
        System.out.println("test1"+(ArrayNode) dashboardNode.path("dashboard").path("panels"));

        //updated

        if (dashboardNode.path("dashboard").has("rows")) {
            panelsNode =(ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
            System.out.println("panelsnode1"+panelsNode);
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
            System.out.println("panelsnode2"+panelsNode);
        }


        System.out.println("panelsNode"+panelsNode);
        boolean panelDeleted = false;
        for (int i = 0; i < panelsNode.size(); i++) {
            JsonNode panelNode = panelsNode.get(i);
            if (panelNode.get("id").asText().equals(panelId)) {
                if (panelsNode.has(i)) {
                    panelsNode.remove(i);
                    panelDeleted = true;
                    break;
                }
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
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // get dashboard JSON from Grafana
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // Update the dashboard JSON pour le new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode;

        if (dashboardNode.path("dashboard").path("panels").isArray()) {
            panelsNode =(ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        }

        for (JsonNode panelNode : panelsNode) {
            if (panelNode.path("title").asText().equals(panelTitle)) {
                ((ObjectNode) panelNode).setAll(updatedPanel);
                break;
            }
        }

        // Send the updated JSON to Grafana
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
        //  dashboard JSON
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);
        //     System.out.println(dashboardJson);

        //kifkif lehne à verifier l'arborescence taa l champs panels
        //  JsonNode dashboardNode = mapper.readTree(dashboardJson).get("dashboard");

        // JsonNode dashboardNode = mapper.readTree(dashboardJson).get("dashboard").get("rows").get(0);
        //   System.out.println(dashboardNode);
        //  System.out.println(dashboardNode.get("dashboard").get("panels"));
        // Find the panel to modify based on the panel ID

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode;

        if (dashboardNode.path("dashboard").path("panels").isArray()) {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        }


        JsonNode panelNode = null;
        for (JsonNode panel : panelsNode) {
            if (panel.get("id").asInt() == panelId) {
                panelNode = panel;
                System.out.println(panelNode); //OK

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

        String modifiedDashboardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);
        grafanaClient.updateDashboard(modifiedDashboardJson);

    }


    // mazelt masta3malthech fl front
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

        // dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

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
        JsonNode panelsNode;
                //= dashboardNode.get("dashboard").get("panels");


        if(dashboardNode.get("dashboard").get("panels").isEmpty())
        {
            panelsNode=dashboardNode.get("dashboard").get("rows").get(0).get("panels");
        }
        else {
            panelsNode=dashboardNode.get("dashboard").get("panels");

        }

        for (JsonNode panelNode : panelsNode) {
            if (panelNode.get("id").asText().equals(panelId)) {
             //   System.out.println(panelNode.toString());
                return panelNode;
            }
        }

        throw new RuntimeException("Panel not found with title: " + panelId);
    }
    public String getPanelIdByTitle(String dashboardTitle, String panelTitle) throws IOException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        String dashboardUid = grafanaClient.getDashboardUidByTitle(dashboardTitle);

        // Search for l panels in the dashboard
        ResponseEntity<String> panelSearchResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardUid, HttpMethod.GET, requestEntity, String.class);
        JsonNode dashboardJson = new ObjectMapper().readTree(panelSearchResponse.getBody());
        JsonNode panels = dashboardJson.at("/dashboard/panels");
        System.out.println(panels);

        for (JsonNode panel : panels) {
            if (panel.get("title").asText().equals(panelTitle)) {
                System.out.println(panel.get("id").asText());
                return panel.get("id").asText();
            }
        }
        throw new RuntimeException("Panel not found: " + panelTitle);
    }



}
