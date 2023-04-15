package com.grafana.expo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class DashboardBuilder { //service

    public String buildDashboard(String title, String[] targets) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("title", title);
        System.out.println("l title" + title);
        System.out.println("mrigl fl dashboard builder");

        ArrayNode rowsNode = objectMapper.createArrayNode();
        ObjectNode rowNode = objectMapper.createObjectNode();
        ArrayNode panelsNode = objectMapper.createArrayNode();

        for (int i = 0; i < targets.length; i++) {
            ObjectNode panelNode = objectMapper.createObjectNode();
            panelNode.put("title", "Panel " + (i + 1));
            panelNode.put("type", "graph");
            panelNode.put("datasource", "Prometheus");
            panelNode.put("id", i + 1);

            ArrayNode targetsNode = objectMapper.createArrayNode();
            ObjectNode targetNode = objectMapper.createObjectNode();
            targetNode.put("expr", targets[i]);
            targetsNode.add(targetNode);

            panelNode.set("targets", targetsNode);
            panelsNode.add(panelNode);
            System.out.println(panelNode);
        }

        rowNode.set("panels", panelsNode);
        rowsNode.add(rowNode);
        rootNode.set("rows", rowsNode);
        System.out.println("l json" + objectMapper.writeValueAsString(rootNode));
        return objectMapper.writeValueAsString(rootNode);
    }




}
