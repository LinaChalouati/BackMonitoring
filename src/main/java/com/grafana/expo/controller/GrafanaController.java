package com.grafana.expo.controller;

import com.grafana.expo.model.GrafanaDashboardResponse;
import com.grafana.expo.model.GrafanaExportRequest;
import com.grafana.expo.service.GrafanaService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.json.JSONObject;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class GrafanaController {

    private final GrafanaService grafanaService;

    @Autowired
    public GrafanaController(GrafanaService grafanaService) {
        this.grafanaService = grafanaService;
    }

    @PostMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<byte[]>> exportDashboard(@RequestBody GrafanaExportRequest exportRequest) {
        return grafanaService.exportDashboard(exportRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/dashboard/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GrafanaDashboardResponse>> getDashboard(@PathVariable String uid) {
        return grafanaService.getDashboard(uid)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/dashboard/{uid}/widget/{panelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getWidget(@PathVariable String uid, @PathVariable String panelId) {
        return grafanaService.getDashboard(uid)
                .map(GrafanaDashboardResponse::getDashboardJson)
                .map(jsonString -> {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    if (!jsonObject.has("panels")) {
                        return ResponseEntity.notFound().build();
                    }
                    JSONArray panels = jsonObject.getJSONArray("panels");
                    for (int i = 0; i < panels.length(); i++) {
                        JSONObject panel = panels.getJSONObject(i);
                        if (panel.getString("id").equals(panelId)) {
                            return ResponseEntity.ok().body(panel.toString());
                        }
                    }
                    return ResponseEntity.notFound().build();
                });
    }


}
