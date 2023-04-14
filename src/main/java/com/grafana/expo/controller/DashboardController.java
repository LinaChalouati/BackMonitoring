package com.grafana.expo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.grafana.expo.model.DashboardBuilder;
import com.grafana.expo.model.GrafanaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    @PostMapping("/panel")
    public void addPanel(@RequestParam(value = "dashboardTitle") String dashboardTitle,
                         @RequestParam(value = "target") String targetExpr) throws IOException {

        String dashboardId = grafanaClient.getDashboardIdByTitle(dashboardTitle);
        System.out.println(dashboardId);
        if (dashboardId == null) {
            throw new RuntimeException("Dashboard not found");
        }

        grafanaClient.addPanel(dashboardTitle, targetExpr);
    }


}
