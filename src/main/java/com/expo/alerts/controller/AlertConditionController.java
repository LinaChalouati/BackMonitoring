package com.expo.alerts.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


//l partie hedhi bch tetbadel , à savoir , instead i'll be using prometheus for the alertes

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

