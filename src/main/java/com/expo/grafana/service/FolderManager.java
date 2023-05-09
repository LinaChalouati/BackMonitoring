package com.expo.grafana.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class FolderManager {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey ;

    private final RestTemplate restTemplate;

    public FolderManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String checkIfFolderExists() {
        String folderTitle="templates";
        // Check if folder already exists
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                grafanaUrl + "api/search?type=dash-folder&query=" + folderTitle,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(apiKey)),
                String.class
        );
        System.out.println(responseEntity);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JsonNode result = readTree(responseEntity.getBody());
            if (result.size() > 0) {
                // Folder already exists
                System.out.println(result.get(0).get("uid").asText());
                return result.get(0).get("uid").asText();
            }
        }

        // Folder does not exist, create it
        String folderJson = String.format("{\"title\": \"%s\"}", folderTitle);
        responseEntity = restTemplate.exchange(
                grafanaUrl + "/api/folders",
                HttpMethod.POST,
                new HttpEntity<>(folderJson, createHeaders(apiKey)),
                String.class
        );
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to create folder: " + responseEntity.getBody());
        }

        // Folder created successfully, return its UID
        JsonNode result = readTree(responseEntity.getBody());
        return result.get("uid").asText();
    }

    private HttpHeaders createHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    private JsonNode readTree(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }
}
