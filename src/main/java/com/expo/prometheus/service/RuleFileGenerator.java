package com.expo.prometheus.service;
import com.expo.prometheus.model.AlertInfo;
import com.expo.prometheus.model.RuleInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class RuleFileGenerator {
    private static final String RULES_FILE_NAME = "alert.rules.yml";
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";


    @Value("${prometheus.server.url}")
    private String prometheusServerurl;
    @Value("${prometheus.config.path}")
    private String prometheusConfigPath;
    private String theLocalRulesFile=RESOURCES_DIRECTORY+RULES_FILE_NAME;
    private PrometheusAlertService prometheusAlertService;
    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;



    public RuleFileGenerator(PrometheusAlertService prometheusAlertService) {
        this.prometheusAlertService = prometheusAlertService;
    }

    public void generateRuleFile() throws IOException {

        // Generate the rule file content
        StringBuilder ruleFileContent = new StringBuilder();

        ruleFileContent.append("groups:\n");
        ruleFileContent.append("- name: Prometheus alerts\n");
        ruleFileContent.append("  rules:\n");


        // Write the rule f  resources directory
        Files.writeString(Path.of(theLocalRulesFile), ruleFileContent.toString());
    }


    public void addRuleToFile(String alertname, List<String> instances, String metric, String severity, String comparaison, String value, String time, String summary, String description) throws IOException {
        try {
            String rule = generateRule(alertname, metric, instances, severity, comparaison, value, time, summary, description);
            Path ruleFilePath = Path.of(theLocalRulesFile);

            if (Files.exists(ruleFilePath)) {
                Files.writeString(ruleFilePath, rule + "\n", StandardOpenOption.APPEND);
                System.out.println("Rule added to the file successfully.");
                this.prometheusAlertService.pushRuleFile(theLocalRulesFile, "rule");
            } else {
                System.err.println("Rule File generated");
                generateRuleFile();
                addRuleToFile(alertname,instances,metric,severity,comparaison,value,time,summary,description);

                //Files.writeString(ruleFilePath, rule + "\n", StandardOpenOption.APPEND);
              //  this.prometheusAlertService.pushRuleFile(theLocalRulesFile, "rule");
            }
        } catch (IOException e) {
            System.err.println("Failed to add the rule to the file: " + e.getMessage());
        }
    }

    private String generateRule(String alertname, String metric, List<String> instances, String severity, String comparison, String value, String time, String summary, String description) {
        // Generate the rule content for the given metric and instances
        String comparisonCharacter = getComparisonCharacter(comparison);
        StringBuilder ruleBuilder = new StringBuilder();

        for (String instance : instances) {
            ruleBuilder.append(String.format(
                    "  - alert: %s\n" +
                            "    expr: %s{instance=\"%s\"} %s %s \n" +
                            "    for: %s \n" +
                            "    labels:\n" +
                            "      severity: %s\n" +
                            "      instance: %s\n" +
                            "    annotations:\n" +
                            "      summary: %s \n" +
                            "      description: %s \n" +
                            "\n",
                    alertname, metric, instance, comparisonCharacter, value, time, severity, instance, summary, description));
        }

        return ruleBuilder.toString();
    }
    public String getComparisonCharacter(String comparaison){
        if(comparaison.equals("equal")){
            return "=";
        }
        if(comparaison.equals("sup")){
            return ">";
        }
        if(comparaison.equals("inf")){
            return "<";
        }
        return "";
    }
    public String getDescription(String comparaisoncarac){
        if(comparaisoncarac.equals(">")){
            return "High";
        }
        if(comparaisoncarac.equals("<")){
            return "Low";
        }
        if(comparaisoncarac.equals("=")){
            return "Equal Value of";
        }
        return "";
    }


    // do i need to modify the expression ?
    //A voir later on
    //mazeli l severity
    public boolean modifyRule(String ruleName, String property, String newValue,String instance) {
        try {
            //hedhi à discuter

          //  System.out.println("here");

            // Read the file as a YAML file
            Yaml yaml = new Yaml();
            Object yamlObject = yaml.load(new FileInputStream(theLocalRulesFile));
        //    System.out.println("yamlObject" + yamlObject);

            // Find the rule to modify
            List<Map<String, Object>> rules = (List<Map<String, Object>>) ((Map<String, Object>) yamlObject).get("groups");
         //   System.out.println("lena3"+rules);

            boolean ruleFound = false;

            for (Map<String, Object> rule : rules) {
                List<Map<String, Object>> ruleList = (List<Map<String, Object>>) rule.get("rules");
            //    System.out.println("ruleList"+ruleList);

                for (Map<String, Object> ruleItem : ruleList) {
                    Map<String, Object>  item= (Map<String, Object> ) ruleItem.get("labels");

                    if (ruleItem.get("alert") != null && ruleItem.get("alert").equals(ruleName)&& item.get("instance").equals(instance)) {
                     //   System.out.println("lena4"+ruleItem);
                        if (property.equals("severity")){
                            Map<String,Object> labels= (Map<String, Object>) ruleItem.get("labels");
                            labels.put(property, newValue);
                     //       System.out.println("labels"+labels);
                            ruleFound = true;

                            break;

                        }
                        if (property.equals("summary")){
                            Map<String,Object> annotations= (Map<String, Object>) ruleItem.get("annotations");
                            annotations.put(property, newValue);
                       //     System.out.println("labels"+annotations);
                            ruleFound = true;

                            break;

                        }
                        if (property.equals("description")){
                            Map<String,Object> annotations= (Map<String, Object>) ruleItem.get("annotations");
                            annotations.put(property, newValue);
                            System.out.println("labels"+annotations);
                            ruleFound = true;

                            break;

                        }
                        else{
                            ruleItem.put(property, newValue);
                            ruleFound = true;
                            break;

                        }
                        // Modify the property

                    }
                }
                if (ruleFound) {
                    break;
                }
            }
            System.out.println("lena3"+rules);
            // Check if the rule exists
            if (!ruleFound) {
                // The rule does not exist
                return false;
            }
            yaml.dump(yamlObject, new FileWriter(theLocalRulesFile));
              System.out.println("hereeeeee"+theLocalRulesFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static final String PROMETHEUS_API_URL = "http://localhost:9090/api/v1/rules";
    public List<RuleInfo> getRulesByInstance(String instance) throws JsonProcessingException {
        // Set the headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Send the GET request to retrieve the rules
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                PROMETHEUS_API_URL,
                HttpMethod.GET,
                null,
                String.class
        );
        System.out.println(response);

        // Check if the request was successful
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
//parse
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode groupsNode = root.get("data").get("groups");

            //  rule information
            List<RuleInfo> ruleInfoList = new ArrayList<>();

            for (JsonNode groupNode : groupsNode) {
                JsonNode rulesNode = groupNode.get("rules");
                System.out.println("rulesnode"+rulesNode);

                for (JsonNode ruleNode : rulesNode) {
                    String name = ruleNode.get("name").asText();
                    String query = ruleNode.get("query").asText();
                    String duration = ruleNode.get("duration").asText();
                    String state = ruleNode.get("state").asText();
                    String ruleInstance = ruleNode.get("labels").get("instance").asText();
                    String severity = ruleNode.get("labels").get("severity").asText();
                    String description =ruleNode.get("annotations").get("description").asText();
                    String summary =ruleNode.get("annotations").get("summary").asText();




                    // Check if the rule instance matches the desired instance
                   if (ruleInstance.equals(instance)) {
                        // Create a RuleInfo object with the extracted information
                        RuleInfo ruleInfo = new RuleInfo(instance,name, query, duration, state,description,summary,severity);
                        ruleInfoList.add(ruleInfo);
                        System.out.println("ruleinfo"+ruleInfo);
                    }
                }
            }

            // Return the list of rule information
            return ruleInfoList;
        } else {
            System.out.println("Failed to retrieve the rules. Response: " + response.getBody());
            return null;
        }
    }

    // A voir later on
    public boolean deleteAlertRule(String alertName) {
        try {
            // Create the request URL to delete the alert rule
            String deleteUrl = PROMETHEUS_API_URL + "?alertname=" + alertName;

            // Set the headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Send the DELETE request to delete the alert rule
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response;
            response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

            // Check if the deletion was successful
            if (response.getStatusCode().is2xxSuccessful()) {
                return true;
            } else {
                System.out.println("Failed to delete the alert rule. Response: " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean deleteRule(String ruleName,String instance) {
        try {
            //hedhi à discuter

            System.out.println("here");

            // Read the file as a YAML file
            Yaml yaml = new Yaml();
            Object yamlObject = yaml.load(new FileInputStream(theLocalRulesFile));
            System.out.println("yamlObject" + yamlObject);

            // Find the rule to modify
            List<Map<String, Object>> rules = (List<Map<String, Object>>) ((Map<String, Object>) yamlObject).get("groups");
            System.out.println("lena3"+rules);

            boolean ruleFound = false;

            for (Map<String, Object> rule : rules) {
                List<Map<String, Object>> ruleList = (List<Map<String, Object>>) rule.get("rules");
                System.out.println("ruleList"+ruleList);

                for (Map<String, Object> ruleItem : ruleList) {
                    System.out.println("ruleitem"+(Map<String, Object> )ruleItem.get("labels"));
                    Map<String, Object>  item= (Map<String, Object> ) ruleItem.get("labels");
                    System.out.println("item"+item.get("instance"));
                    if (ruleItem.get("alert") != null && ruleItem.get("alert").equals(ruleName) && item.get("instance").equals(instance) ) {
                        ruleList.remove(ruleItem);
                        System.out.println("ok lenaaa");

                        ruleFound = true;
                        break;

                    }
                    // Modify the property


                }
                if (ruleFound) {
                    break;
                }
            }
            System.out.println("lena3"+rules);
            // Check if the rule exists
            if (!ruleFound) {
                // The rule does not exist
                return false;
            }
            yaml.dump(yamlObject, new FileWriter(theLocalRulesFile));
            System.out.println("hereeeeee"+theLocalRulesFile);


            this.prometheusAlertService.pushRuleFile(theLocalRulesFile,"rule");

            this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AlertInfo> getAlertStatus(String instance) {
        try {
            String alertStatusEndpoint = prometheusServerurl + "/api/v1/alerts";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(alertStatusEndpoint, HttpMethod.GET, null, String.class);

            // Check if the request was successful
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                System.out.println("fl get alert status "+responseBody);
                // Parse the response body as JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode alertsNode = objectMapper.readTree(responseBody).get("data").get("alerts");

                List<AlertInfo> filteredAlerts = new ArrayList<>();

                for (JsonNode alertNode : alertsNode) {
                    String alertInstance = alertNode.get("labels").get("instance").asText();

                    if (alertInstance.equals(instance)) {
                        AlertInfo alertInfo = new AlertInfo();

                        alertInfo.setAlertname(alertNode.get("labels").get("alertname").asText());
                        alertInfo.setInstance(alertNode.get("labels").get("instance").asText());
                        alertInfo.setJob(alertNode.get("labels").get("job").asText());
                        alertInfo.setSeverity(alertNode.get("labels").get("severity").asText());
                        alertInfo.setState(alertNode.get("state").asText());
                        alertInfo.setActiveAt(alertNode.get("activeAt").asText());

                        filteredAlerts.add(alertInfo);
                    }
                }

                // Return the filtered alerts
                return filteredAlerts;
            } else {
                System.out.println("Failed to retrieve alert status. Response: " + response.getBody());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to fetch alert status.");
            return null;
        }
    }

    public String ExpressionGenerator(String expression){


        return "";
    }

}






