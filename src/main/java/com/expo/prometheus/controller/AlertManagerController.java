package com.expo.prometheus.controller;

import com.expo.prometheus.service.AlertFileGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AlertManagerController {
    private final AlertFileGenerator alertFileGenerator;

    public AlertManagerController(AlertFileGenerator alertFileGenerator) {
        this.alertFileGenerator = alertFileGenerator;
    }

    @GetMapping("/generate-alert-file")
    public ResponseEntity<String> generateAlertFile() throws IOException {
        alertFileGenerator.generateConfigFile();
        String message = "Alert file generated successfully.";
        return ResponseEntity.ok(message);
    }

//aandi mochkla fl mails
    @PostMapping("/add-alert-receiver")
    public String addAlertReceiver(
            @RequestParam(value="receiverName")String receiverName,
            @RequestParam(value="receiverEmail") List<String> receiverEmail,
            @RequestParam(value="senderEmail")String senderEmail,
            @RequestParam(value="smarthost")String smarthost

            ) throws IOException {
        String username="";
        String password="";
        System.out.println("receiverEmail"+receiverEmail);
        System.out.println("receiverEmail"+receiverEmail.get(0));
        System.out.println("receiverEmail"+receiverEmail.get(1));

        alertFileGenerator.addReceiverToFile(receiverName,receiverEmail,senderEmail,smarthost,username,password);
        return "Alert file generated successfully.";
    }
    //fiha mochkla
    @GetMapping("/add-new-receiver")
    public String addNewtReceiver(
            @RequestParam(value="receiverName")String receiverName,
            @RequestParam(value="receiverEmail") String receiverEmail



    ) throws IOException {
        alertFileGenerator.addEmailToReceiver(receiverName,receiverEmail);
        return "Alert file generated successfully.";
    }
    //ok
    @PostMapping("/update-receiverName")
    public String updateReceiverName(
            @RequestParam(value="currentReceiverName")String currentReceiverName,
            @RequestParam(value="newReceiverName") String newReceiverName



    ) throws IOException {
        alertFileGenerator.updateReceiverName(currentReceiverName,newReceiverName);
        return "Alert file generated successfully.";
    }
    /*@GetMapping("/get_alert_by_rule")
    public List<String> getAlertByRule(@RequestParam(value="rulename")String ruleName){
        return "";
    }*/


}
