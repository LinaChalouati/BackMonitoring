package com.expo.prometheus.controller;

import com.expo.prometheus.model.MailNotifInfo;
import com.expo.prometheus.service.AlertFileGenerator;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.ok("Alert file generated successfully.");
    }
    @PostMapping("/add-route")
    public ResponseEntity<String> addRoute(
            @RequestParam("alertname") String alertname,
            @RequestParam("receivername") String receivername,
            @RequestParam("instance") String instance,
            @RequestParam("receiverEmails") List<String> receiverEmails) throws IOException {
        try {
            alertFileGenerator.addRoute(alertname, receivername, instance, receiverEmails);
            return ResponseEntity.ok("Route added successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add route: " + e.getMessage());
        }
    }

    @PostMapping("/add-new-receiver")
    public ResponseEntity<String> addNewReceiver(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverEmail") String receiverEmail) throws IOException {
        alertFileGenerator.addEmailToReceiver(receiverName, receiverEmail);
        return ResponseEntity.ok("New Mail added to Team successfully.");
    }

    @PostMapping("/update-receiverName")
    public ResponseEntity<String> updateReceiverName(
            @RequestParam("currentReceiverName") String currentReceiverName,
            @RequestParam("newReceiverName") String newReceiverName) throws IOException {
        alertFileGenerator.updateReceiverName(currentReceiverName, newReceiverName);
        return ResponseEntity.ok("Receiver Name updated successfully.");
    }


    //plutot moch delete alert khater you can't delete an alert
    @DeleteMapping("/delete-alert")
    public ResponseEntity<String> deleteAlert(@RequestParam(value = "alertname") String alertName,
                                              @RequestParam(value="instance")String instance) throws IOException {
        boolean deleted = alertFileGenerator.deleteAlert(alertName,instance);
        if (deleted) {
            return ResponseEntity.ok("Alert deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alert not found.");
        }
    }

    @PostMapping("/disable-alert")
    public ResponseEntity<String> disableAlert(@RequestParam(value = "alertname") String alertName,
                                               @RequestParam(value = "instance")String instance) throws IOException {
        boolean disabled = alertFileGenerator.disableAlert(alertName,instance);
        if (disabled) {
            return ResponseEntity.ok("Alert disabled successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alert not found.");
        }
    }

    @DeleteMapping("/delete-email-alert")
    public ResponseEntity<String> deleteEmailFromReceiver(@RequestParam(value = "receiverName") String receiverName,
                                              @RequestParam(value="receiverEmail")String receiverEmail) throws IOException {
        boolean deleted = alertFileGenerator.deleteEmailFromReceiver(receiverName,receiverEmail);
        if (deleted) {
            return ResponseEntity.ok("Email deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alert not found.");
        }
    }


    @GetMapping("/get-alert-emails")
    public ResponseEntity<MailNotifInfo> getAlertEmails(
            @RequestParam(value = "alertName") String alertName,
            @RequestParam(value = "instance") String instance) throws IOException {

        MailNotifInfo mailNotifInfo = alertFileGenerator.getEmailsByAlert(alertName, instance);

        if (mailNotifInfo == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(mailNotifInfo);
        }
    }


}
