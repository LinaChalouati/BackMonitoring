package com.expo.prometheus.controller;

import com.expo.prometheus.model.AlertEmailInfo;
import com.expo.prometheus.model.MailNotifInfo;
import com.expo.prometheus.service.AlertFileGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/alertmanager")
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
    public ResponseEntity<Boolean> addRoute(
            @RequestParam("alertname") String alertname,
            @RequestParam("receivername") String receivername,
            @RequestParam("instance") String instance,
            @RequestParam("receiverEmails") List<String> receiverEmails) {
        try {
            alertFileGenerator.addRoute(alertname, receivername, instance, receiverEmails);
            return ResponseEntity.ok(true);
        } catch (IOException e) {
            String errorMessage = "Failed to add route: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
    @PostMapping("/add-team")
    public ResponseEntity<Boolean> addTeam(
            @RequestParam("alertname") String alertname,
            @RequestParam("receivername") String receivername,
            @RequestParam("instance") String instance) {
        try {
            alertFileGenerator.addTeam(alertname, receivername, instance);
            return ResponseEntity.ok(true);
        } catch (IOException e) {
            String errorMessage = "Failed to add route: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }


    @PostMapping("/add-new-receiver")
    public ResponseEntity<Boolean> addNewReceiver(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverEmails") List<String> receiverEmails) throws IOException {
       Boolean add= alertFileGenerator.addEmailsToReceiver(receiverName, receiverEmails);
        if(add){
            return ResponseEntity.ok(true);

        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);

    }

    // manich bch nestaamelha khater chen3a
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

    @PostMapping("/disable-enable-alert")
    public ResponseEntity<Boolean> disableAlert(@RequestParam(value = "alertname") String alertName,
                                               @RequestParam(value = "instance")String instance) throws IOException {
        boolean disabled = alertFileGenerator.disableEnableAlert(alertName,instance);
        System.out.println("disabled"+disabled);
        if (disabled) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }

    @DeleteMapping("/delete-email-alert")
    public ResponseEntity<Boolean> deleteEmailFromReceiver(@RequestParam(value = "receiverName") String receiverName,
                                              @RequestParam(value="receiverEmail")String receiverEmail) throws IOException {
        boolean deleted = alertFileGenerator.deleteEmailFromReceiver(receiverName,receiverEmail);
        if (deleted) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }


    @GetMapping("/get-alert-emails")
    public ResponseEntity<MailNotifInfo> getAlertEmails(
            @RequestParam(value = "alertname") String alertName,
            @RequestParam(value = "instance") String instance) throws IOException {

        MailNotifInfo mailNotifInfo = alertFileGenerator.getEmailsByAlert(alertName, instance);

        if (mailNotifInfo == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(mailNotifInfo);
        }
    }
    @GetMapping("/get-instance-alert-details")
    public ResponseEntity<List<AlertEmailInfo>> getAlertsAndEmailsByInstance(
            @RequestParam(value = "instance") String instance) throws IOException {

        List<AlertEmailInfo> alertEmailInfo = alertFileGenerator.getAlertsAndEmailsByInstance(instance);

        if (alertEmailInfo == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(alertEmailInfo);
        }
    }

}
