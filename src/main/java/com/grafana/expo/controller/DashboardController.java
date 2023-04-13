package com.grafana.expo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.grafana.expo.model.DashboardBuilder;
import com.grafana.expo.model.GrafanaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class DashboardController {

    @Autowired
    private DashboardBuilder dashboardBuilder;

    @Autowired
    private GrafanaClient grafanaClient;

   /* @GetMapping("/dashboard")
    public String getDashboard(@RequestParam(value = "title") String title,
                               @RequestParam(value = "targets") String[] targets) throws JsonProcessingException {
        return dashboardBuilder.buildDashboard(title, targets);
    }

    @PostMapping("/dashboard")
    public void createDashboard(@RequestBody String jsonPayload) throws JsonProcessingException {
        grafanaClient.createDashboard(jsonPayload);
    }*/
   @PostMapping("/dashboard")
   public void createDashboard(@RequestParam(value = "title") String title,
                               @RequestParam(value = "targets") String[] targets) throws JsonProcessingException {
       String jsonPayload = dashboardBuilder.buildDashboard(title, targets);
       grafanaClient.createDashboard(jsonPayload);
   }

}
