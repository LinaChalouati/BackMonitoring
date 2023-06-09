package com.expo.prometheus.service;

import com.expo.prometheus.model.K8sClusterQuery;
import com.expo.prometheus.model.OtherQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PrometheusQuery {
    K8sClusterQuery VmQuery=  new K8sClusterQuery();


    Map<String ,String> VmMap=VmQuery.getQuery();
    K8sClusterQuery K8sQuery=  new K8sClusterQuery();
    Map<String ,String> K8sMap=K8sQuery.getQuery();

    OtherQuery OtherQuery=new OtherQuery();
    Map<String ,String> OtherQueryMap=OtherQuery.getQuery();
    public String prometheus_url="http://172.18.3.220:9090/";


    public String getQueryExpression(String indiceexpr, String ip, String port) throws JsonProcessingException {
        System.out.println("lena");

        String instance = ip + ":" + port;
        System.out.println("targetindice"+indiceexpr);

        System.out.println("instance"+instance);

        if (OtherQueryMap.containsKey(indiceexpr)) {
            String expr=OtherQueryMap.get(indiceexpr);
            expr = expr.replaceAll("%s", instance);

            return expr;
        } else {
            String wheredeployment = getDeploymentWhere(ip, port);

        /*   if (wheredeployment.equals("VM") && VmMap.containsKey(indiceexpr)) {
                 String expr=VmMap.get(indiceexpr);
                 expr = expr.replaceAll("%s", instance);
                return expr;
            }*/

            if (wheredeployment.equals("K8s Cluster") && K8sMap.containsKey(indiceexpr)) {
                String expr = String.format(K8sMap.get(indiceexpr), instance);
                return expr;
            }
        }

        return "";
    }


    //@Value("${prometheus.server.url}")
    //private String prometheus_url;
    public String getDeploymentWhere(String ip,String port) throws JsonProcessingException {
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
                return "K8s Cluster";
            }

            else {
                return "VM";
            }


        }

        return "";

    }
}
