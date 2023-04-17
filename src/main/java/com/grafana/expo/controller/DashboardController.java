package com.grafana.expo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.grafana.expo.service.DashboardBuilder;
import com.grafana.expo.service.GrafanaClient;
import com.grafana.expo.service.PanelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
public class DashboardController {

    @Autowired
    private DashboardBuilder dashboardBuilder;

    @Autowired
    private GrafanaClient grafanaClient;
    @Autowired
    private PanelClient panelClient;

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
        System.out.println(PanelTitle);

       /* In case of l input dashboardid and not l title
        String dashboardId = grafanaClient.getDashboardIdByTitle(dashboardTitle);
        System.out.println(dashboardId);
        if (dashboardId == null) {
            throw new RuntimeException("Dashboard not found");
        }
*/
        panelClient.addPanel(dashboardTitle,PanelTitle, targetExpr);
    }
    @PostMapping("/deletePanel")
    public void deletePanel(@RequestParam (value="dashboardTitle") String dashboardTitle, @RequestParam (value="PanelTitle")String panelTitle) throws JsonProcessingException{

        panelClient.deletePanel(dashboardTitle, panelTitle);

    }
    @PostMapping("/deleteDashboard")
    public void deleteDashboard(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException{

        grafanaClient.deleteDashboard(dashboardTitle);

    }
    @PostMapping("/modifyDashboard")
    public void updateDashboard(@RequestParam (value="dashboardTitle") String dashboardTitle,@RequestParam (value="newTitle") String newTitle,@RequestParam (value="newDescription") String newDescription,@RequestParam (value="tags") List<String> tags) throws JsonProcessingException{

       // grafanaClient.deleteDashboard(dashboardTitle);
        grafanaClient.modifyDashboard(dashboardTitle,newTitle,newDescription,tags);

    }
    @PostMapping("/updatePanel")
    public void updatePanel(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException{

        // grafanaClient.deleteDashboard(dashboardTitle);

    }
        @PostMapping("/getpanels")
    public void getPanels(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException, UnsupportedEncodingException {

        panelClient.GetPanels(dashboardTitle);

    }
    @PostMapping("/getdashboard")
    public void getDashboard(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException, UnsupportedEncodingException {

        grafanaClient.GetDashboard(dashboardTitle);

    }
    @PostMapping("/updatepanel")
    public void updatePanel(@RequestParam (value="dashboardTitle")String dashboardTitle,@RequestParam (value="panelTitle") String panelTitle, @RequestParam (value="updatedPanel")ObjectNode updatedPanel) throws JsonProcessingException {
        panelClient.updatePanel(dashboardTitle, panelTitle, updatedPanel);
    }
    @PostMapping("/modifypanel")
    public void modifyPanel(@RequestParam (value="dashboardTitle")String dashboardTitle,@RequestParam (value="panelTitle") String panelTitle, @RequestParam (value="updatedPanel")ObjectNode updatedPanel) throws JsonProcessingException {
        panelClient.updatePanel(dashboardTitle, panelTitle, updatedPanel);
    }


}
