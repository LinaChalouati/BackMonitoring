package com.expo.grafana.service;

import com.expo.grafana.model.GrafanaDashboardResponse;
import com.expo.grafana.model.GrafanaExportRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GrafanaService {

    private final WebClient webClient;
    private final String authToken;
    private final String uid;
    private static final String GRAFANA_API_EXPORT  = "http://localhost:3000/api/dashboards/uid/grafexport";
    private static final String GRAFANA_API_DASHBOARD = "http://localhost:3000/api/dashboards/uid/{uid}";

    public GrafanaService(WebClient.Builder webClientBuilder, @Value("${grafana.authToken}") String authToken, @Value("${grafana.uid}") String uid) {
        this.webClient = webClientBuilder.build();
        this.authToken = authToken;
        this.uid = uid;
    }

    public Mono<byte[]> exportDashboard(GrafanaExportRequest exportRequest) {
        return webClient.post()
                .uri(GRAFANA_API_EXPORT)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken))
                .body(BodyInserters.fromValue(exportRequest))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Mono<GrafanaDashboardResponse> getDashboard(String uid) {
        String apiURL = GRAFANA_API_DASHBOARD.replace("{uid}", uid);
        return webClient.get()
                .uri(apiURL)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(authToken))
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonString -> {
                    GrafanaDashboardResponse response = new GrafanaDashboardResponse();
                    response.setDashboardUid(uid);
                    response.setDashboardJson(jsonString);
                    return response;
                });
    }



}