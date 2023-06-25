package com.expo.prometheus.service;
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



        // Write the rule file to the resources directory
        Files.writeString(Path.of(theLocalRulesFile), ruleFileContent.toString());
    }


    public void addRuleToFile( String alertname,String instance,String metric,String severity,String comparaison,String value,String time,String summary,String descrption) throws IOException {
        String rule = generateRule(alertname,metric, instance,severity,comparaison,value,time,summary,descrption);

        try {
            Path ruleFilePath = Path.of(theLocalRulesFile);

            if (Files.exists(ruleFilePath)) {
                Files.writeString(ruleFilePath, rule + "\n", StandardOpenOption.APPEND);
                System.out.println("Rule added to the file successfully.");
                this.prometheusAlertService.pushRuleFile(theLocalRulesFile,"rule");

            } else {
                //   System.err.println("Rule file does not exist. Please generate the rule file first.");
                System.err.println("Rule File generated");

                generateRuleFile();
                Files.writeString(ruleFilePath, rule + "\n", StandardOpenOption.APPEND);
                this.prometheusAlertService.pushRuleFile(theLocalRulesFile,"rule");




            }
        } catch (IOException e) {
            System.err.println("Failed to add the rule to the file: " + e.getMessage());
        }


    }


    private String generateRule(String alertname,String metric, String instance, String severity, String comparison,String value,String time,String summary,String description) {
// Generate the rule content for the given metric and instance
        String comparisonCharacter = getComparisonCharacter(comparison);
        //String summaryNotice=getDescription(comparisonCharacter);
        String rule = String.format(
                "  - alert: %s\n" +
                        "    expr: %s{instance=\"%s\"}  %s %s \n" +
                        "    for: %s \n" +
                        "    labels:\n" +
                        "      severity: %s\n" +
                        "      instance: %s\n" +

                        "    annotations:\n" +
                        "      summary: %s \n" +
                        "      description: %s \n"+"\n",
                alertname,metric, instance, comparisonCharacter, value,time,severity,instance,summary, description);

        return rule;
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
    public boolean modifyRule(String ruleName, String property, String newValue) {
        try {
            //hedhi à discuter
            String prometheusfile = prometheusConfigPath + "alert.rules.yml";

            System.out.println("here");

            // Read the file as a YAML file
            Yaml yaml = new Yaml();
            Object yamlObject = yaml.load(new FileInputStream(prometheusfile));
            System.out.println("yamlObject" + yamlObject);

            // Find the rule to modify
            List<Map<String, Object>> rules = (List<Map<String, Object>>) ((Map<String, Object>) yamlObject).get("groups");
            System.out.println("lena3"+rules);

            boolean ruleFound = false;

            for (Map<String, Object> rule : rules) {
                List<Map<String, Object>> ruleList = (List<Map<String, Object>>) rule.get("rules");
                System.out.println("ruleList"+ruleList);

                for (Map<String, Object> ruleItem : ruleList) {
                    if (ruleItem.get("alert") != null && ruleItem.get("alert").equals(ruleName)) {
                        System.out.println("lena4"+ruleItem);
                        if (property.equals("severity")){
                            Map<String,Object> labels= (Map<String, Object>) ruleItem.get("labels");
                            labels.put(property, newValue);
                            System.out.println("labels"+labels);
                            ruleFound = true;

                            break;

                        }
                        if (property.equals("summary")){
                            Map<String,Object> annotations= (Map<String, Object>) ruleItem.get("annotations");
                            annotations.put(property, newValue);
                            System.out.println("labels"+annotations);
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
            // Write the modified content back to the configuration file
            yaml.dump(yamlObject, new FileWriter(theLocalRulesFile));
            System.out.println("hereeeeee"+theLocalRulesFile);
            this.prometheusAlertService.pushRuleFile(theLocalRulesFile,"rule");

            // Execute the command to restart or reload Prometheus
            this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);
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

            // Parse the JSON response and extract the rule information
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode groupsNode = root.get("data").get("groups");

            // Create a list to store the rule information
            List<RuleInfo> ruleInfoList = new ArrayList<>();

            // Iterate over the rule groups
            for (JsonNode groupNode : groupsNode) {
                JsonNode rulesNode = groupNode.get("rules");
                System.out.println("rulesnode"+rulesNode);

                // Iterate over the rules and extract relevant information
                for (JsonNode ruleNode : rulesNode) {
                    String name = ruleNode.get("name").asText();
                    String query = ruleNode.get("query").asText();
                    String duration = ruleNode.get("duration").asText();
                    String state = ruleNode.get("state").asText();
                    String ruleInstance = ruleNode.get("labels").get("instance").asText();

                    // Check if the rule instance matches the desired instance
                   if (ruleInstance.equals(instance)) {
                        // Create a RuleInfo object with the extracted information
                        RuleInfo ruleInfo = new RuleInfo(name, query, duration, state);
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
    /*

    public RuleInfo getRuleByName(String ruleName) throws JsonProcessingException {
        // Set the headers for the request
        List<RuleInfo> rulesinfo=getRules();
            // Iterate over the rules and search for the rule with the specified name
            for (RuleInfo ruleNode : rulesinfo) {
                String name = ruleNode.getName();
                if (name.equals(ruleName)) {
                    // Extract relevant information for the matching rule
                    String query = ruleNode.getQuery();
                    String duration = ruleNode.getDuration();
                    String state = ruleNode.getState();


                    // Create a RuleInfo object with the extracted information
                    RuleInfo ruleInfo = new RuleInfo(name, query, duration,state);
                    return ruleInfo;
                }
            }

            // No rule with the specified name found
            System.out.println("Rule with name '" + ruleName + "' not found.");
            return null;

    }*/
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

    public boolean deleteRule(String ruleName) {
        try {
            //hedhi à discuter
            String prometheusfile = prometheusConfigPath + "alert.rules.yml";

            System.out.println("here");

            // Read the file as a YAML file
            Yaml yaml = new Yaml();
            Object yamlObject = yaml.load(new FileInputStream(prometheusfile));
            System.out.println("yamlObject" + yamlObject);

            // Find the rule to modify
            List<Map<String, Object>> rules = (List<Map<String, Object>>) ((Map<String, Object>) yamlObject).get("groups");
            System.out.println("lena3"+rules);

            boolean ruleFound = false;

            for (Map<String, Object> rule : rules) {
                List<Map<String, Object>> ruleList = (List<Map<String, Object>>) rule.get("rules");
                System.out.println("ruleList"+ruleList);

                for (Map<String, Object> ruleItem : ruleList) {
                    if (ruleItem.get("alert") != null && ruleItem.get("alert").equals(ruleName)) {
                        ruleList.remove(ruleItem);
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
            // Write the modified content back to the configuration file
            yaml.dump(yamlObject, new FileWriter(theLocalRulesFile));
            System.out.println("hereeeeee"+theLocalRulesFile);


            this.prometheusAlertService.pushRuleFile(theLocalRulesFile,"rule");

            // Execute the command to restart or reload Prometheus
            this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public JsonNode getAlertStatus(String instance) {
        try {
            String alertStatusEndpoint = prometheusServerurl + "/api/v1/alerts";

            // Send the GET request to retrieve alert status
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(alertStatusEndpoint, HttpMethod.GET, null, String.class);

            // Check if the request was successful
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();

                // Parse the response body as JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode alertsNode = objectMapper.readTree(responseBody).get("data").get("alerts");

                // Create a JSON array to store the filtered alerts
                JsonNode filteredAlerts = objectMapper.createArrayNode();

                // Process each alert and add the desired fields to the filtered alerts
                for (JsonNode alertNode : alertsNode) {
                    // Get the instance label value
                    String alertInstance = alertNode.get("labels").get("instance").asText();

                    // Compare the instance label with the provided instance input
                    if (alertInstance.equals(instance)) {
                        // Create a JSON object for the filtered alert
                        JsonNode filteredAlert = objectMapper.createObjectNode();

                        // Add the desired fields to the filtered alert
                        ((ObjectNode) filteredAlert).put("alertname", alertNode.get("labels").get("alertname").asText());
                        ((ObjectNode) filteredAlert).put("instance", alertNode.get("labels").get("instance").asText());
                        ((ObjectNode) filteredAlert).put("job", alertNode.get("labels").get("job").asText());
                        ((ObjectNode) filteredAlert).put("severity", alertNode.get("labels").get("severity").asText());
                        ((ObjectNode) filteredAlert).put("state", alertNode.get("state").asText());
                        ((ObjectNode) filteredAlert).put("activeAt", alertNode.get("activeAt").asText());

                        // Add the filtered alert to the filtered alerts array
                        ((ArrayNode) filteredAlerts).add(filteredAlert);
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






