package com.expo.prometheus.service;

import com.expo.prometheus.model.K8sClusterQuery;
import com.expo.prometheus.model.OtherQuery;
import com.expo.prometheus.model.QueryInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PrometheusQuery {
    K8sClusterQuery VmQuery = new K8sClusterQuery();


    Map<String, String> VmMap = VmQuery.getQuery();
    K8sClusterQuery K8sQuery = new K8sClusterQuery();
    Map<String, String> K8sMap = K8sQuery.getQuery();

    OtherQuery OtherQuery = new OtherQuery();
    Map<String, String> OtherQueryMap = OtherQuery.getQuery();
    public String prometheus_url = "http://localhost:9090/";


    public String getQueryExpression(String indiceexpr, String ip, String port) throws JsonProcessingException {
        System.out.println("lena");

        String instance = ip + ":" + port;
        System.out.println("targetindice" + indiceexpr);

        System.out.println("instance" + OtherQueryMap.containsKey(indiceexpr));

        if (OtherQueryMap.containsKey(indiceexpr)) {
            System.out.println("lenaa fl query");
            String expr = OtherQueryMap.get(indiceexpr);
            expr = expr.replaceAll("%s", instance);

            return expr;
        } else {
            String wheredeployment = getDeploymentWhere(ip, port);


            if (wheredeployment.equals("K8s Cluster") && K8sMap.containsKey(indiceexpr)) {
                String expr = String.format(K8sMap.get(indiceexpr), instance);
                return expr;
            }
        }

        return "";
    }


    //@Value("${prometheus.server.url}")
    //private String prometheus_url;
    public String getDeploymentWhere(String ip, String port) throws JsonProcessingException {
        String instanceName = ip + ":" + port;
        //nchouf les instances lkol f prometheus
        String url = prometheus_url + "api/v1/targets";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();

        JsonNode root = new ObjectMapper().readTree(body);
        JsonNode targets = root.path("data");
        JsonNode targets2 = targets.get("activeTargets");

        System.out.println("targets" + targets2);

        String ScrapeUrl = null;
        //nlawej aala l url taa les metriques taa l instance eli 7ajti biha
        for (JsonNode searchtarget : targets2) {
            if (searchtarget.path("labels").get("instance").asText().equals(instanceName)) {
                ScrapeUrl = (searchtarget.path("scrapeUrl").asText());
            }
        }


        System.out.println("ScrapeUrl" + ScrapeUrl);
        Map<String, String> deployment = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();

        //nchouf  est ce que vm wala k8s cluster
        if (ScrapeUrl != "") {
            RestTemplate restTemplate2 = new RestTemplate();
            ResponseEntity<String> responsemetrics = restTemplate.getForEntity(ScrapeUrl, String.class);
            String metrics = responsemetrics.getBody();
            if (metrics.contains("kubernetes") || metrics.contains("pod") || metrics.contains("replicas")
                    || metrics.contains("node")) {
                return "K8s Cluster";
            } else {
                return "VM";
            }


        }

        return "";

    }

    // A VOIIIIIIIIIIIIR parceque somemetrics mayest7a9ouch braces



    public QueryInfo getInstanceMetrics(List<String> instances) throws Exception {
        // Create JSON object to hold metric data
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        QueryInfo queryInfo = new QueryInfo();

        // Create set to hold unique metric names
        Set<String> metricNamesSet = new HashSet<>();

        // Iterate over the list of instances
        for (String instance : instances) {
            String instanceName = instance;

            // Get active targets from Prometheus
            String url = prometheus_url + "api/v1/targets";
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();

            JsonNode root = new ObjectMapper().readTree(body);
            JsonNode targets = root.path("data");
            JsonNode targets2 = targets.get("activeTargets");

            String scrapeUrl = null;

            // Find the scrape URL for the instance
            for (JsonNode searchtarget : targets2) {
                if (searchtarget.path("labels").get("instance").asText().equals(instanceName)) {
                    scrapeUrl = searchtarget.path("scrapeUrl").asText();
                    break;  // Exit the loop if the instance is found
                }
            }

            // Fetch metrics from the instance
            if (scrapeUrl != null && !scrapeUrl.isEmpty()) {
                RestTemplate restTemplate2 = new RestTemplate();
                ResponseEntity<String> responseMetrics = restTemplate2.getForEntity(scrapeUrl, String.class);
                String metrics = responseMetrics.getBody();

                // Split metrics into lines and process each line
                String[] lines = metrics.split("\\r?\\n");
                for (String line : lines) {
                    // Ignore comments and empty lines
                    if (!line.startsWith("#") && !line.isEmpty()) {
                        // Split each line into metric name, value, and labels
                        String[] parts = line.split("\\s+");
                        String nameWithLabels = parts[0];
                        String value = parts[1];
                        String labels = "";
                        if (parts.length > 2) {
                            labels = parts[2];
                        }

                        // Remove labels from metric name
                        int openBraceIndex = nameWithLabels.indexOf("{");
                        String name = nameWithLabels;
                        if (openBraceIndex >= 0) {
                            name = nameWithLabels.substring(0, openBraceIndex);
                        }
                        name = name.replaceAll("_", " ");

                        // Remove application and id labels from name
                        name = name.replaceAll("\\{application=\"[^\"]*\",id=\"[^\"]*\",?\\}", "");

                        // Remove any remaining curly braces or commas from name
                        name = name.replaceAll("[{}]", "");
                        name = name.replaceAll(",", " ");

                        // Add metric name to set
                        metricNamesSet.add(name);

                        // Create JSON object for metric and add to result
                        ObjectNode metricNode = mapper.createObjectNode();
                        String expression = nameWithLabels;
                        if (openBraceIndex >= 0) {
                            expression = nameWithLabels.substring(0, openBraceIndex) ;
                        }
                        metricNode.put("expression", expression);
                        result.set(name, metricNode);

                        // Add metric name and expression to the query map
                        queryInfo.getQueryMap().put(name, expression);
                    }
                }
            }
        }

        // Convert set to array
        ArrayNode metricNames = mapper.createArrayNode();
        for (String name : metricNamesSet) {
            metricNames.add(name);
        }

        // Create JSON object to hold metrics and metric names
        ObjectNode responseNode = mapper.createObjectNode();
        responseNode.set("metrics", result);
        responseNode.set("metricNames", metricNames);

        return queryInfo;
    }

    public QueryInfo getCommonInstanceMetrics(List<String> instances) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        QueryInfo queryInfo = new QueryInfo();

        // Create a list of sets to hold metric names for each instance
        List<Set<String>> metricNamesSets = new ArrayList<>();

        // Iterate over the list of instances
        for (String instance : instances) {
            String instanceName = instance;

            // Get active targets from Prometheus
            String url = prometheus_url + "api/v1/targets";
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();

            JsonNode root = new ObjectMapper().readTree(body);
            JsonNode targets = root.path("data");
            JsonNode targets2 = targets.get("activeTargets");

            String scrapeUrl = null;

            // Find the scrape URL for the instance
            for (JsonNode searchtarget : targets2) {
                if (searchtarget.path("labels").get("instance").asText().equals(instanceName)) {
                    scrapeUrl = searchtarget.path("scrapeUrl").asText();
                    break;  // Exit the loop if the instance is found
                }
            }

            // Fetch metrics from the instance
            if (scrapeUrl != null && !scrapeUrl.isEmpty()) {
                RestTemplate restTemplate2 = new RestTemplate();
                ResponseEntity<String> responseMetrics = restTemplate2.getForEntity(scrapeUrl, String.class);
                String metrics = responseMetrics.getBody();

                // Split metrics into lines and process each line
                String[] lines = metrics.split("\\r?\\n");
                Set<String> metricNamesSet = new HashSet<>();
                for (String line : lines) {
                    // Ignore comments and empty lines
                    if (!line.startsWith("#") && !line.isEmpty()) {
                        // Split each line into metric name, value, and labels
                        String[] parts = line.split("\\s+");
                        String nameWithLabels = parts[0];
                        String value = parts[1];
                        String labels = "";
                        if (parts.length > 2) {
                            labels = parts[2];
                        }

                        // Remove labels from metric name
                        int openBraceIndex = nameWithLabels.indexOf("{");
                        String name = nameWithLabels;
                        if (openBraceIndex >= 0) {
                            name = nameWithLabels.substring(0, openBraceIndex);
                        }
                        name = name.replaceAll("_", " ");

                        // Remove application and id labels from name
                        name = name.replaceAll("\\{application=\"[^\"]*\",id=\"[^\"]*\",?\\}", "");

                        // Remove any remaining curly braces or commas from name
                        name = name.replaceAll("[{}]", "");
                        name = name.replaceAll(",", " ");

                        // Add metric name to set
                        metricNamesSet.add(name);

                        // Create JSON object for metric and add to result
                        ObjectNode metricNode = mapper.createObjectNode();
                        String expression = nameWithLabels;
                        if (openBraceIndex >= 0) {
                            expression = nameWithLabels.substring(0, openBraceIndex) ;
                        }
                        metricNode.put("expression", expression);
                        result.set(name, metricNode);
                    }
                }

                // Add the metric names set to the list
                metricNamesSets.add(metricNamesSet);
            }
        }

        // Find the common metric names by comparing the sets
        Set<String> commonMetricNamesSet = new HashSet<>(metricNamesSets.get(0));
        for (Set<String> metricNamesSet : metricNamesSets) {
            commonMetricNamesSet.retainAll(metricNamesSet);
        }

        // Convert the common metric names set to a list
        List<String> commonMetricNames = new ArrayList<>(commonMetricNamesSet);

        // Populate the query map with common metric names and expressions
        for (String metricName : commonMetricNames) {
            String expression = result.get(metricName).get("expression").asText();
            queryInfo.getQueryMap().put(metricName, expression);
        }

        return queryInfo;
    }






}
