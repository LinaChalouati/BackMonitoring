package com.expo.prometheus.controller;

import com.expo.prometheus.service.AlertFileGenerator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AlertManagerController {
    private final AlertFileGenerator alertFileGenerator;

    public AlertManagerController(AlertFileGenerator alertFileGenerator) {
        this.alertFileGenerator = alertFileGenerator;
    }

    @GetMapping("/generate-alert-file")
    public String generateAlertFile() throws IOException {
        alertFileGenerator.generateConfigFile();
        return "Alert file generated successfully.";
    }

    @GetMapping("/add-alert-receiver")
    public String addAlertReceiver(
            @RequestParam(value="receiverName")String receiverName,
            @RequestParam(value="receiverEmail")String receiverEmail,
            @RequestParam(value="senderEmail")String senderEmail,
            @RequestParam(value="smarthost")String smarthost,
            @RequestParam(value="username")String username,
            @RequestParam(value="password")String password


            ) throws IOException {
        alertFileGenerator.addReceiverToFile(receiverName,receiverEmail,senderEmail,smarthost,username,password);
        return "Alert file generated successfully.";
    }
}
