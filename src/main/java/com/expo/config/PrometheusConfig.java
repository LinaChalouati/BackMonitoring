package com.expo.config;

import com.expo.grafana.service.GrafanaClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrometheusConfig {

    @Value("${prometheus.server.url}")
    private String prometheusServerUrl;

    @Value("${grafana.url}")
    private String grafanaServerUrl;
    @Value("${grafana.apiKey}")
    private String apiKey;
    @Value("${prometheus.server.url}")
    private String PrometheusServerUrl;

    private HttpEntity<String> getHeaderHttp(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        return requestEntity;

    }
    public void addPrometheus(String name, String url) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("name", name);
        rootNode.put("type", "prometheus");
        rootNode.put("url", url);
       // rootNode.put("basicAuth", false);
        rootNode.put("access","proxy");
        System.out.println(rootNode);
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), updateHeaders);

        ResponseEntity<String> updateResponse = restTemplate.exchange(
                grafanaServerUrl + "api/datasources",
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Datasource added successfully");
        } else {
            throw new RuntimeException("Datasource add failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }



    }
    public void getDataSourceHealth(String name) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        String uidDatasource=this.getDataSourceUidByName(name);
        ResponseEntity<String> response = restTemplate.exchange(grafanaServerUrl + "api/datasources/uid/"+uidDatasource+
                "/health", HttpMethod.GET, requestEntity, String.class);
        JsonNode health = new ObjectMapper().readTree(response.getBody());
        System.out.println(health);

    }
    public String getDataSourceIdByName(String name) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(grafanaServerUrl + "api/datasources", HttpMethod.GET, requestEntity, String.class);
        JsonNode datasourcesJson = new ObjectMapper().readTree(response.getBody());
        JsonNode datasources = datasourcesJson.isArray() ? datasourcesJson : datasourcesJson.at("/");

        for (JsonNode datasource : datasources) {
            if (datasource.get("name").asText().equals(name)) {
                return datasource.get("id").asText();
            }
        }

        throw new RuntimeException("Datasource not found: " + name);
    }
    public String getDataSourceUidByName(String name) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(grafanaServerUrl + "api/datasources", HttpMethod.GET, requestEntity, String.class);
        JsonNode datasourcesJson = new ObjectMapper().readTree(response.getBody());
        JsonNode datasources = datasourcesJson.isArray() ? datasourcesJson : datasourcesJson.at("/");

        for (JsonNode datasource : datasources) {
            if (datasource.get("name").asText().equals(name)) {
                return datasource.get("uid").asText();
            }
        }

        throw new RuntimeException("Datasource not found: " + name);
    }

    public JsonNode getDataSourceByName(String name) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        String uidDatasource=this.getDataSourceUidByName(name);
        System.out.println("uidDatasource"+uidDatasource);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(grafanaServerUrl + "api/datasources/uid/"+uidDatasource, HttpMethod.GET, requestEntity, String.class);
        JsonNode datasourcesJson = new ObjectMapper().readTree(response.getBody());
        return datasourcesJson;

    }
    public void modifyDataSource(String name,String newurl,String version,String newname) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        String uidDatasource=this.getDataSourceUidByName(name);
        System.out.println("uidDatasource"+uidDatasource);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(grafanaServerUrl + "api/datasources/uid/"+uidDatasource, HttpMethod.GET, requestEntity, String.class);
        JsonNode datasourcesJson = new ObjectMapper().readTree(response.getBody());
        if(newurl !=null && !newurl.isEmpty())
        {
            ((ObjectNode) datasourcesJson).put("url",newurl);
        }
        if(version !=null && !version.isEmpty())
        {
            ((ObjectNode) datasourcesJson).put("version",version);
        }
        if(newname !=null && !newname.isEmpty())
        {
            ((ObjectNode) datasourcesJson).put("name",newname);
        }
        ((ObjectNode) datasourcesJson).remove("version");
        System.out.println(datasourcesJson);
        // Le update
       HttpHeaders headers=this.getHeaders();
       String url = grafanaServerUrl+"/api/datasources/";
        HttpEntity<String> requestEntity2 = new HttpEntity<String>("{\"datasource\":" + datasourcesJson + "}", headers);
        System.out.println("requestEntity2"+requestEntity2);
        ResponseEntity<String> response2 = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
        if (response2.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to update dashboard: " + response.getBody());
        }



    }
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }
    public void deleteDataSource(String name) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        String uidDatasource=this.getDataSourceUidByName(name);
        System.out.println("uidDatasource"+uidDatasource);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(grafanaServerUrl + "api/datasources/uid/"+uidDatasource, HttpMethod.DELETE, requestEntity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Datasource deleted successfully");
            ResponseEntity.ok("deleted");

        } else {
            throw new RuntimeException("Datasource delete failed: " + response.getStatusCodeValue() + " " + response.getBody());
        }

    }



}
