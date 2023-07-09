package com.expo.alerts.service;


import org.springframework.stereotype.Service;

//l partie hedhi bch tetbadel , à savoir , instead i'll be using prometheus for the alertes

@Service
public class AlertConditionService {

/*
    @Value("${grafana.apiKey}")
    private String apiKey;

    @Value("${grafana.api.url}")
    private String grafanaApiUrl;

    @Autowired
    private GrafanaClient grafanaClient;
    @Autowired
    private PanelClient panelClient;

    private HttpEntity<String> getHeaderHttp() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

  /*  public void createAlert(AlertCondition alertCondition, String dashboardTitle, String panelTitle) {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        // l url taa l creation of les alertes
        String url=grafanaApiUrl + "/api/alerts";




    }*/



}
