package com.grafana.expo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.grafana.expo.model.Dashboard;
import com.grafana.expo.model.DashboardBuilder;
import com.grafana.expo.model.GrafanaClient;
import com.grafana.expo.model.GrafanaPanel;
import com.grafana.expo.repo.DashboardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.lang.annotation.Target;

@Service
public class DashboardService {

    private GrafanaClient grafanaClient;
    private DashboardBuilder dashboardBuilder;
    private DashboardRepository dashboardRepository;

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.apiKey}")
    private String apiKey;

    public DashboardService(GrafanaClient grafanaClient, DashboardBuilder dashboardBuilder,
                            DashboardRepository dashboardRepository) {
        this.grafanaClient = grafanaClient;
        this.dashboardBuilder = dashboardBuilder;
        this.dashboardRepository = dashboardRepository;
    }

   /* public void createDashboard(String title, String[] targets) throws JsonProcessingException {
        String jsonPayload = dashboardBuilder.buildDashboard(title, targets);
        grafanaClient.createDashboard(jsonPayload);

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle(title);
        dashboard.setJsonPayLoad(jsonPayload);
        dashboardRepository.save(dashboard);
        System.out.println("l dashbooooard aa 7ajj"+dashboard);
    }*/




}
