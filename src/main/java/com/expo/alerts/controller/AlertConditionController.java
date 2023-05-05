package com.expo.alerts.controller;

import com.expo.alerts.model.AlertCondition;
import com.expo.alerts.model.GrafanaAlert;
import com.expo.alerts.service.AlertConditionService;
import com.expo.alerts.service.GrafanaAlertService;
import com.expo.grafana.service.GrafanaClient;
import com.expo.grafana.service.PanelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertConditionController {

   /* @Autowired
    private GrafanaAlertService grafanaAlertService;

    @PostMapping("/createAlert")
    public ResponseEntity<String> createAlert(@RequestParam(value = "alertName") String alertname,
                                              @RequestParam(value = "alertMessage") String alertmessage,
                                              @RequestParam(value = "frequency") int frequency,
                                              @RequestParam(value = "forrange") int forrange,
                                              @RequestParam(value = "DashboardTitle") String DashboardTitle,
                                              @RequestParam(value = "PanelTitle") String PanelTitle
                                              ) {

        AlertCondition alertCondition=new AlertCondition(alertname,alertmessage,frequency,forrange,DashboardTitle,PanelTitle);
  //      AlertConditionService.createAlert(alertCondition);
        //return ;


    }*/
}

