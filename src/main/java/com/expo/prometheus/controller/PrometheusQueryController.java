// PrometheusQueryController.java
package com.expo.prometheus.controller;

import com.expo.prometheus.model.PrometheusResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PrometheusQueryController {
    @Value("${prometheus.server.url}")
    private String prometheus_url;

    @GetMapping("/metrics")
    public String getMetrics(@RequestParam("ip") String ip, @RequestParam("port") String port) {
        String url = "http://" + ip + ":" + port + "/api/prometheus/metrics"; //ken na7i api/prometheus w n7ot metrics toul temchi in somecase
        System.out.println(url);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        return response;
    }
    @GetMapping("/allmetrics")
    public String getAllMetrics(@RequestParam("ip") String ip, @RequestParam("port") String port) throws JsonProcessingException {
        String job = this.getJobName(ip, port);
        System.out.println(job);
        String url = prometheus_url + "api/v1/query?query=" + job;
        System.out.println(url);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String response = restTemplate.getForObject(url, String.class);

        return response;
    }

    @GetMapping("/getjob")
    public String getJobName(@RequestParam("ip") String ip, @RequestParam("port") String port) throws JsonProcessingException {
        String url = prometheus_url+ "api/v1/targets";
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        System.out.println(response);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode activeTargetsNode = rootNode.get("data").get("activeTargets");

        for (JsonNode targetNode : activeTargetsNode) {
            String instance = targetNode.get("labels").get("instance").asText();
            System.out.println(instance);

            if (instance.equals(ip + ":" + port)) {
                JsonNode jobNameNode = targetNode.get("labels").get("job");
                String jobName = jobNameNode.asText();
                System.out.println(jobName);
                return jobName;
            }
        }

        return "Job not found";
    }

}


