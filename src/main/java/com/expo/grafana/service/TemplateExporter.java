package com.expo.grafana.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class TemplateExporter {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey ;

    private final RestTemplate restTemplate;
    @Autowired
    private FolderManager folderManager;

    public TemplateExporter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }
    public Boolean checkTemplateExistence()
    {  // Check if folder exists
        String folderUid =folderManager.checkIfFolderExists();
        if (folderUid == null) {
            return false;
        }

        // Check if dashboard exists in folder
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                grafanaUrl + "/api/search?type=dash-db&folderIds=" + folderUid + "&query=template",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(apiKey)),
                String.class
        );
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonNode result = readTree(responseEntity.getBody());
            if (result.size() > 0) {
                return true;
            }
        }
        return false;
    }


    public void exportDashboard() throws IOException {
        String dashboardJson = loadDashboardJson();
      //  System.out.println(dashboardJson);
        String folderUid=folderManager.checkIfFolderExists();
        dashboardJson = addFolderUidToDashboardJson(dashboardJson, folderUid);
System.out.println("dashboardJson"+dashboardJson);

        HttpHeaders headers = createHeaders(apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(dashboardJson, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, requestEntity, String.class);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to export dashboard: " + responseEntity.getBody());
        }
    }

    private String loadDashboardJson() throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of("src/main/resources/template.json"));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }
    private String addFolderUidToDashboardJson(String dashboardJson, String folderUid) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dashboardNode = objectMapper.readTree(dashboardJson);

        // Create a new ObjectNode with the folderUid field
        ObjectNode folderUidNode = objectMapper.createObjectNode();
        folderUidNode.put("folderUid", folderUid);

        // Merge the new ObjectNode with the existing dashboardNode
        ObjectNode updatedDashboardNode = (ObjectNode) dashboardNode;
        updatedDashboardNode.set("folderUid", folderUidNode.get("folderUid"));
        System.out.println(updatedDashboardNode);
        return objectMapper.writeValueAsString(updatedDashboardNode);
    }
    private JsonNode readTree(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }

}
