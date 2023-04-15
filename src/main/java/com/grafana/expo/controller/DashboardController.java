package com.grafana.expo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.grafana.expo.service.DashboardBuilder;
import com.grafana.expo.service.GrafanaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class DashboardController {

    @Autowired
    private DashboardBuilder dashboardBuilder;

    @Autowired
    private GrafanaClient grafanaClient;

   @PostMapping("/dashboard")
       public void createDashboard(@RequestParam(value = "title") String title,
                                   @RequestParam(value = "targets") String[] targets) throws JsonProcessingException {
           String jsonPayload = dashboardBuilder.buildDashboard(title, targets);
           grafanaClient.createDashboard(jsonPayload);
           System.out.println(jsonPayload);
       }
    @PostMapping("/panel")
    public void addPanel(@RequestParam(value = "dashboardTitle") String dashboardTitle,@RequestParam(value = "PanelTitle") String PanelTitle,
                         @RequestParam(value = "target") String targetExpr) throws IOException {

       /* In case of l input dashboardid and not l title
        String dashboardId = grafanaClient.getDashboardIdByTitle(dashboardTitle);
        System.out.println(dashboardId);
        if (dashboardId == null) {
            throw new RuntimeException("Dashboard not found");
        }
*/
        grafanaClient.addPanel(dashboardTitle,PanelTitle, targetExpr);
    }
    @PostMapping("/deletePanel")
    public void deletePanel(@RequestParam (value="dashboardTitle") String dashboardTitle, @RequestParam (value="PanelTitle")String panelTitle) throws JsonProcessingException{

        grafanaClient.deletePanel(dashboardTitle, panelTitle);

    }
    @PostMapping("/deleteDashboard")
    public void deleteDashboard(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException{

        grafanaClient.deleteDashboard(dashboardTitle);

    }
    @PostMapping("/updateDashboard")
    public void updateDashboard(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException{

       // grafanaClient.deleteDashboard(dashboardTitle);

    }
    @PostMapping("/updatePanel")
    public void updatePanel(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException{

        // grafanaClient.deleteDashboard(dashboardTitle);

    }
}
