// PrometheusQueryController.java
package com.expo.prometheus.controller;

import com.expo.prometheus.model.OtherQuery;
import com.expo.prometheus.model.QueryInfo;
import com.expo.prometheus.service.PrometheusQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class PrometheusQueryController {
    //@Value("${prometheus.server.url}")
    //private String prometheus_url;
    private String prometheus_url="http://localhost:9090/";
    public PrometheusQuery prometheusQuery;


    public PrometheusQueryController(PrometheusQuery prometheusQuery) {
        this.prometheusQuery = prometheusQuery;
    }

    @GetMapping("/metrics")
    public String getMetrics(@RequestParam("ip") String ip, @RequestParam("port") String port) {
        String url = "http://" + ip + ":" + port + "/api/prometheus/metrics"; //ken na7i api/prometheus w n7ot metrics toul temchi in somecase
        System.out.println(url);

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        return response;
    }
    @GetMapping("/allmetrics")
    public JsonNode getAllMetrics(@RequestParam("ip") String ip, @RequestParam("port") String port) throws JsonProcessingException {
        String instanceName=ip+":"+port;
        //nchouf les instances lkol f prometheus
        String url = prometheus_url+"api/v1/targets";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();

        JsonNode root = new ObjectMapper().readTree(body);
        JsonNode targets = root.path("data");
        JsonNode targets2 = targets.get("activeTargets");

        System.out.println("targets"+targets2);

        String ScrapeUrl = null;
        //nlawej aala l url taa les metriques taa l instance eli 7ajti biha
        for (JsonNode searchtarget : targets2) {
            if(searchtarget.path("labels").get("instance").asText().equals(instanceName)){
                ScrapeUrl= (searchtarget.path("scrapeUrl").asText());
            }
        }


        System.out.println("ScrapeUrl"+ScrapeUrl);
        Map<String, String> deployment = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();

        //nchouf  est ce que vm wala k8s cluster
        if(ScrapeUrl!=""){
            RestTemplate restTemplate2 = new RestTemplate();
            ResponseEntity<String> responsemetrics = restTemplate.getForEntity(ScrapeUrl, String.class);
            String metrics = responsemetrics.getBody();
            if(metrics.contains("kubernetes")|| metrics.contains("pod")||metrics.contains("replicas")
                    ||metrics.contains("node"))
            {
                ((ObjectNode) node).put("Deployment", "K8s Cluster");

                // return "K8s";
            }

            else {
                ((ObjectNode) node).put("Deployment", "VM");
            }


        }

        return node;

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
    @GetMapping("/query_expr")
    public String getTheExpr(@RequestParam("indiceexpr")String indiceexpr,@RequestParam("ip") String ip, @RequestParam("port") String port) throws JsonProcessingException {
        OtherQuery OtherQuery=new OtherQuery();
        Map<String ,String> OtherQueryMap=OtherQuery.getQuery();
        PrometheusQuery prometheusQuery = new PrometheusQuery();

        System.out.println("targetindice"+indiceexpr);
        System.out.println("ip"+ip);
        System.out.println("port"+port);

        String expr=prometheusQuery.getQueryExpression(indiceexpr, ip, port);
        System.out.println("expr"+expr);
        if(expr!=""){
            return expr;
        }
        return "";

    }


    @GetMapping("/check_deployment")
    public JsonNode checkDeployment(@RequestParam("ip") String ip, @RequestParam("port") String port) throws JsonProcessingException {
        String instanceName=ip+":"+port;
        //nchouf les instances lkol f prometheus
        String url = prometheus_url+"api/v1/targets";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();

        JsonNode root = new ObjectMapper().readTree(body);
        JsonNode targets = root.path("data");
        JsonNode targets2 = targets.get("activeTargets");

        System.out.println("targets"+targets2);

        String ScrapeUrl = null;
        //nlawej aala l url taa les metriques taa l instance eli 7ajti biha
        for (JsonNode searchtarget : targets2) {
            if(searchtarget.path("labels").get("instance").asText().equals(instanceName)){
                ScrapeUrl= (searchtarget.path("scrapeUrl").asText());
            }
        }


        System.out.println("ScrapeUrl"+ScrapeUrl);
        Map<String, String> deployment = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.createObjectNode();

        //nchouf  est ce que vm wala k8s cluster
        if(ScrapeUrl!=""){
            RestTemplate restTemplate2 = new RestTemplate();
            ResponseEntity<String> responsemetrics = restTemplate.getForEntity(ScrapeUrl, String.class);
            String metrics = responsemetrics.getBody();
            if(metrics.contains("kubernetes")|| metrics.contains("pod")||metrics.contains("replicas")
                    ||metrics.contains("node"))
            {
                ((ObjectNode) node).put("Deployment", "K8s Cluster");

                // return "K8s";
            }

            else {
                    ((ObjectNode) node).put("Deployment", "VM");
            }


        }

        return node;

    }

    // c bon resolved

    // A VOIIIIIIIIIIIIR parceque somemetrics mayest7a9ouch braces

    @GetMapping("/instance_metrics")
    public QueryInfo getInstanceMetrics(@RequestParam("instances") List<String> instance) throws Exception {
        System.out.println("here");
        if(instance.size()==1){
            return  this.prometheusQuery.getInstanceMetrics(instance);

        }
        if(instance.size()>1){
            return this.prometheusQuery.getCommonInstanceMetrics(instance);

        }
        return null;
    }
}


