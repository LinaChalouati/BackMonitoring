package com.expo.grafana.controller;

import com.expo.grafana.service.DashboardBuilder;
import com.expo.grafana.service.GrafanaClient;
import com.expo.grafana.service.OverViewPanelsService;
import com.expo.grafana.service.PanelClient;
import com.expo.prometheus.service.PrometheusQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


//kiff kiff à verifier le path du panels here
// f actia temchili .get("dashboard").get(panels) f pc mteei .get("rows").get(0).get("panels") c donc à verifier aalech (tested 06 Mai 2023)
//ps il y'avait un changement de la version du grafana que j'utilise donc peut etre l json à générer tbadlet l format mteeo

//@RestController
@RequestMapping("/api/grafana")
@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private DashboardBuilder dashboardBuilder;

    @Autowired
    private GrafanaClient grafanaClient;
    @Autowired
    private PanelClient panelClient;
    @Autowired
    private PrometheusQuery prometheusQuery;
    private final OverViewPanelsService overViewPanelsService;

    public DashboardController(OverViewPanelsService overViewPanelsService) {
        this.overViewPanelsService = overViewPanelsService;
    }


    @PostMapping("/dashboard")
    public ResponseEntity<?> createDashboard(@RequestParam(value = "title") String projectName,
                                             @RequestParam(value = "targets") String[] targets) throws JsonProcessingException {
        // Use the projectName as the title
        String title = projectName;

        if (grafanaClient.doesDashboardExist(title)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dashboard already exists");
        }

        String jsonPayload = dashboardBuilder.buildDashboard(title, targets);
        grafanaClient.createDashboard(jsonPayload);
        System.out.println(jsonPayload);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    Integer id=1;


    @PostMapping("/panel")
    public void addPanel(@RequestParam(value = "dashboardTitle") String dashboardTitle,@RequestParam(value = "PanelTitle") String PanelTitle,
                         @RequestParam(value = "target") String targetExpr,@RequestParam (value= "panelChart")String chart,
                        @RequestParam(value="ip")String ip,
                         @RequestParam(value="port")String port,@RequestParam(value = "tag")String tag) throws Exception {


        System.out.println(PanelTitle);

      //System.out.println("fl controller"+grafanaClient.getAllPanelIds(dashboardTitle).isEmpty());
        if(! grafanaClient.getAllPanelIds(dashboardTitle).isEmpty()){
            List<String> panels = grafanaClient.getAllPanelIds(dashboardTitle);
            Collections.sort(panels);
            id=Integer.valueOf(panels.get(panels.size() - 1) )+1;
            }
        String target=this.prometheusQuery.getQueryExpression(targetExpr,ip,port);
        int tagnumber = Integer.parseInt(tag);

        panelClient.addPanel(dashboardTitle,PanelTitle, target, chart,id ,tagnumber);
        if(targetExpr.contains("time")){
            panelClient.setFormat(dashboardTitle,id);
            System.out.println("ok mrigl");
        }
    }
    @GetMapping("setunit")
    public void setUnit(@RequestParam(value="dashboardTitle")String title,@RequestParam(value = "id")Integer id) throws Exception {
        panelClient.setFormat(title,id);

    }
    @PostMapping("/deletePanel")
    public void deletePanel(@RequestParam (value="dashboardTitle") String dashboardTitle, @RequestParam (value="PanelTitle")String panelTitle) throws JsonProcessingException{

        panelClient.deletePanel(dashboardTitle, panelTitle);

    }
    @DeleteMapping("/delete-Panel")
    public ResponseEntity<?> deletePanelById(@RequestParam (value="dashboardTitle") String dashboardTitle, @RequestParam (value="PanelId")String panelId) throws JsonProcessingException{

        if (dashboardTitle =="" || panelId=="") {
           // throw new BadRequestException("The dashboard title is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        }


        panelClient.deletePanelById(dashboardTitle, panelId);


        return ResponseEntity.status(HttpStatus.OK).build();
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
    @GetMapping("/get-template-uid")
    public JsonNode getTemplateUidByTitle() throws IOException {
    String Uid= grafanaClient.getDashboardUidByTitle("template");
        ObjectMapper objectMapper=new ObjectMapper();
        ObjectNode rootNode=objectMapper.createObjectNode();
        rootNode.put("uid",Uid);
        JsonNode jsonNode=rootNode;
        return jsonNode;

    }

    @PostMapping("/dashboard-uid")
    public ResponseEntity<JsonNode> getDashboardUid(@RequestParam("dashboardTitle") String dashboardTitle) throws IOException {
        String uid = grafanaClient.getDashboardUidByTitle(dashboardTitle);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        if (uid != null) {
            rootNode.put("uid", uid);
        }
        JsonNode jsonNode = rootNode;
        System.out.println(jsonNode);
        return ResponseEntity.ok(jsonNode);
    }


    @GetMapping("/allpanels")
    public ResponseEntity<List<String>> getAllPanelsId(@RequestParam("dashboardTitle") String title) throws JsonProcessingException {
        List<String> panels = grafanaClient.getAllPanelIds(title);
        if (panels.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(panels);
        }
    }
    @GetMapping("/allpanelsbytag")
    public ResponseEntity<List<String>> getAllPanelsIdByTags(@RequestParam("dashboardTitle") String title,@RequestParam("tag")String tag) throws JsonProcessingException {
        System.out.println("title"+title);
        System.out.println("tag"+tag);

        List<String> panels = grafanaClient.getAllPanelIdsByTag(title,tag);
        if (panels.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(panels);
        }
    }

    @PostMapping("/generic_panel")
    public void addGenericPanel(@RequestParam(value = "dashboardTitle") String dashboardTitle,  @RequestParam(value = "ip") String ip, @RequestParam(value = "port") String port,
                                @RequestParam(value = "appType") String appType) throws Exception {
        System.out.println("hereee");
        System.out.println("dashboardTitle"+dashboardTitle);
        System.out.println("appType"+appType);

        overViewPanelsService.addPanel(dashboardTitle, ip,port, appType);
    }
}
