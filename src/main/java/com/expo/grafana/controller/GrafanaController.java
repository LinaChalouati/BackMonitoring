package com.expo.grafana.controller;

import com.expo.grafana.model.GrafanaDashboardResponse;
import com.expo.grafana.model.GrafanaExportRequest;
import com.expo.grafana.model.GrafanaPanel;
import com.expo.grafana.service.GrafanaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/dashboard")
@Api(tags = {"Grafana Dashboard Controller"})
@CrossOrigin
public class GrafanaController {

    private final GrafanaService grafanaService;

    @Autowired
    public GrafanaController(GrafanaService grafanaService) {
        this.grafanaService = grafanaService;
    }

    @PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Export a Grafana dashboard", response = byte[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Mono<ResponseEntity<byte[]>> exportDashboard(@Valid @RequestBody GrafanaExportRequest exportRequest) {
        return grafanaService.exportDashboard(exportRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping(value = "/dashboard/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a Grafana dashboard by UID", response = GrafanaDashboardResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Mono<ResponseEntity<GrafanaDashboardResponse>> getDashboard(@PathVariable String uid) {
        return grafanaService.getDashboard(uid)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping(value = "/dashboard/{uid}/widget/{panelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> getPanel(@PathVariable String uid, @PathVariable String panelId) {
        return grafanaService.getDashboard(uid)
                .map(GrafanaDashboardResponse::getDashboardJson)
                .map(jsonString -> {
                    JSONObject jsonObject = new JSONObject(jsonString);
            System.out.println("mrigl fl back");
                    if (!jsonObject.has("dashboard")) {
                        return ResponseEntity.notFound().build();
                    }

                    JSONObject dashboard = jsonObject.getJSONObject("dashboard");

                    if (!dashboard.has("panels")) {
                        return ResponseEntity.notFound().build();
                    }

                    JSONArray panels = dashboard.getJSONArray("panels");

                    System.out.println("l9a panel");

                    for (int i = 0; i < panels.length(); i++) {
                        JSONObject panel = panels.getJSONObject(i);
                        System.out.println("l9a panel 1.2");
                        System.out.println(panel.getString("uid"));
                        System.out.println(panelId);

                        if (panel.getString("uid").equals(panelId)) {
                            GrafanaPanel grafanaPanel = new GrafanaPanel();
                            grafanaPanel.setPanelId(panelId);
                            grafanaPanel.setPanelTitle(panel.getString("title"));
                            grafanaPanel.setPanelType(panel.getString("type"));
                           // grafanaPanel.setPanelUrl(panel.getString("url"));
                            System.out.println("l9a panel 2");

                            return ResponseEntity.ok().body(grafanaPanel);
                        }
                    }

                    return ResponseEntity.notFound().build();
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
