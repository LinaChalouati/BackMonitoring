package com.expo.grafana.controller;

import com.expo.grafana.service.DashboardBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.expo.grafana.service.GrafanaClient;
import com.expo.grafana.service.PanelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


//kiff kiff à verifier le path du panels here
// f actia temchili .get("dashboard").get(panels) f pc mteei .get("rows").get(0).get("panels") c donc à verifier aalech (tested 06 Mai 2023)
//ps il y'avait un changement de la version du grafana que j'utilise donc peut etre l json à générer tbadlet l format mteeo

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
    public ResponseEntity<String> modifyDashboard(
            @RequestParam(value = "dashboardTitle") String dashboardTitle,
            @RequestParam(value = "newTitle", required = false) String newTitle,
            @RequestParam(value = "refresh", required = false) String refresh,
            @RequestParam(value = "timeFrom", required = false) String timeFrom,
            @RequestParam(value = "timeTo", required = false) String timeTo,
            @RequestParam(value = "timeRange", required = false) String timeRange) {
        try {
         //   String dashboardUid=grafanaClient.getDashboardUidByTitle(dashboardTitle);
            grafanaClient.modifyDashboard(dashboardTitle,newTitle,refresh,timeFrom,timeTo,timeRange);
            return ResponseEntity.ok("Dashboard updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating dashboard: " + e.getMessage());
        }
    }

        @PostMapping("/getpanels")
    public List<JsonNode> getPanels(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException {

        return  grafanaClient.GetPanels(dashboardTitle);


        }

        @PostMapping("/modifypanel")
    public void modifyPanel(@RequestParam (value="dashboardTitle")String dashboardTitle,@RequestParam (value="panelId") int panelId, @RequestParam (value="newTitle" ,required = false)String newTitle ,@RequestParam (value="newType",required = false)String newType) throws Exception {
        panelClient.modifyPanel(dashboardTitle, panelId, newTitle,newType);
    }
    @PostMapping("/getpanelbyid")
    public JsonNode getPanelById(@RequestParam (value="panelid") String panelId,@RequestParam(value = "dashboardTitle") String dashboardTitle) throws JsonProcessingException {


        return panelClient.getPanelById(panelId,dashboardTitle);
    }
    @PostMapping("/getpanelbytitle")
    public JsonNode getPanelByTitle(@RequestParam (value="dashboardTitle") String dashboardTitle,@RequestParam(value = "panelTitle") String panelTitle) throws JsonProcessingException {
      return  panelClient.getPanelByTitle(dashboardTitle,panelTitle);

    }
    @PostMapping("/getPanelIdByTitle")
        public String getPanelIdByTitle(@RequestParam(value="dashboardTitle") String dashboardTitle , @RequestParam(value = "panelTitle") String panelTitle) throws IOException {
        return panelClient.getPanelIdByTitle(dashboardTitle,panelTitle);
    }
    @PostMapping("/getDashboard")
    public JsonNode findDashbordByTitle(@RequestParam (value="dashboardTitle")String dashboardTitle) throws JsonProcessingException {
     return  grafanaClient.getDashboardByTitle(dashboardTitle);
    }

}
