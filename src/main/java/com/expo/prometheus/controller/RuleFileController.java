package com.expo.prometheus.controller;



import com.expo.prometheus.model.RuleInfo;
import com.expo.prometheus.service.PrometheusAlertService;
import com.expo.prometheus.service.RuleFileGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class RuleFileController {
    private final RuleFileGenerator ruleFileGenerator;
    private final PrometheusAlertService prometheusService;


    @Autowired
    public RuleFileController(RuleFileGenerator ruleFileGenerator, PrometheusAlertService prometheusService) {
        this.ruleFileGenerator = ruleFileGenerator;
        this.prometheusService = prometheusService;
    }

    @GetMapping("/generate-rule-file")
    public String generateRuleFile() throws IOException {
        ruleFileGenerator.generateRuleFile();
        return "Rule file generated successfully.";
    }

    @PostMapping("/add-rule")
    public void addRuleToFile(@RequestParam(value="instance")String instance,@RequestParam(value = "metric")String metric,
    @RequestParam(value="severity")String severity,@RequestParam(value="comparaison")String comparaison,
       @RequestParam(value="value")String value,@RequestParam(value="time")String time){

        ruleFileGenerator.addRuleToFile(instance,metric,severity,comparaison,value,time);


    }




    @PostMapping("/push-rule")
    public String pushRuleFile(@RequestParam("ruleFilePath") String ruleFilePath) {
        try {
            prometheusService.pushRuleFile(ruleFilePath);
            return "Rule file pushed to Prometheus server successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to push the rule file to Prometheus server.";
        }
    }

  /*  @GetMapping("/get_prometheus_rules")
    public JsonNode getPrometheusRules() {
       return ruleFileGenerator.getAlerts();
    }*/

    @PostMapping("/modifyRule")
    public ResponseEntity<Boolean> modifyRule(@RequestParam(value="rulename")String rulename,
                                              @RequestParam(value="property")String property,
                                              @RequestParam(value="newvalue")String newvalue){
        boolean rulemodified=ruleFileGenerator.modifyRule(rulename,property,newvalue);
        if(rulemodified){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(400).body(false);
    }
    @PostMapping("/get_rules")
    public ResponseEntity<List<RuleInfo>> getRules() throws JsonProcessingException {
        List<RuleInfo> rules = ruleFileGenerator.getRules();
        System.out.println("rules"+rules);

        if (!rules.isEmpty()) {
            return ResponseEntity.ok(rules);
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }
    @PostMapping("/get_rule_byname")
    public ResponseEntity<RuleInfo> getRuleByName(@RequestParam(value = "rulename") String rulename) throws JsonProcessingException {
        RuleInfo ruleInfo = ruleFileGenerator.getRuleByName(rulename);

        if (ruleInfo != null) {
            return ResponseEntity.ok(ruleInfo);
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }
    // NEIN
    @PostMapping("/delete_alert")
    public ResponseEntity<Boolean> deleteAlertRule(@RequestParam(value = "rulename") String rulename) throws JsonProcessingException {
        boolean ruleDeleted = ruleFileGenerator.deleteAlertRule(rulename);

        if (ruleDeleted) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(404).body(false);
        }
    }
    @PostMapping("/delete_rule")
    public ResponseEntity<Boolean> deleteRule(@RequestParam(value = "rulename") String rulename) throws JsonProcessingException {
        boolean ruleDeleted = ruleFileGenerator.deleteRule(rulename);

        if (ruleDeleted) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(404).body(false);
        }
    }
    @PostMapping("/get_alert_status")
    public ResponseEntity<JSONArray> getAlerts() throws JsonProcessingException {
        JSONArray alerts = ruleFileGenerator.getAlertStatus();

        if (!alerts.isEmpty()) {
            return ResponseEntity.ok(alerts);
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }



}
