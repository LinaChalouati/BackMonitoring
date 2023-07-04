package com.expo.healthcheck.service;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import java.io.IOException;




@Service
public class AppHealthCheckService {
    private final RestTemplate restTemplate;

    public AppHealthCheckService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public boolean isServiceUp(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean reloadPrometheusAlertManagerServer(String serverUrl) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(serverUrl + "/-/reload");
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                System.out.println("Prometheus reload triggered successfully.");
                return true;
            } else {
                System.out.println("Failed to trigger Prometheus reload. Response Code: " + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}
