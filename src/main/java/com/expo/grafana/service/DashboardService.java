package com.expo.grafana.service;

import com.expo.grafana.repo.DashboardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        System.out.println("l dashbooooard aa "+dashboard);
    }*/




}
