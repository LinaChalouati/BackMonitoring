package com.expo.healthcheck.controller;

import com.expo.healthcheck.service.AppHealthCheckService;
import com.expo.prometheus.service.PrometheusAlertService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.Socket;

//@RestController
@RestController
@RequestMapping("/api/app/healthcheck")
@CrossOrigin("*")
public class AppHealthCheck {
 private AppHealthCheckService checkHealth;
    @Value("${prometheus.server.url}")
    private String prometheusurl;
    @Value("${grafana.url}")
    private String grafanaurl;
    @Value("${alertmanager.url}")
    private String alertmanager;

    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;

    private PrometheusAlertService prometheusAlertService;
    public AppHealthCheck(AppHealthCheckService checkHealth, PrometheusAlertService prometheusAlertService) {
        this.checkHealth = checkHealth;
        this.prometheusAlertService = prometheusAlertService;
    }


        @GetMapping("/health")
        @ResponseStatus(HttpStatus.OK)
        public boolean AppHealthCheck(@RequestParam (name="ipaddr") String ipaddr,@RequestParam (name = "port")String port) {
        System.out.println("OK");
        System.out.println(ipaddr);
            System.out.println(port);

            try (Socket socket = new Socket(ipaddr, Integer.parseInt(port))) {
                System.out.println(socket);
                return true;
            } catch (IOException ex) {
                return false;
            }

    }
    @GetMapping("/prometheus_health")
    public ResponseEntity<Boolean>prometheusHealthCheck() {
        System.out.println("OK");
        boolean isUp=this.checkHealth.isServiceUp(prometheusurl);
        if(isUp){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }
    @GetMapping("/grafana_health")
    public ResponseEntity<Boolean>grafanaHealthCheck() {
        System.out.println("OK");
        boolean isUp=this.checkHealth.isServiceUp(grafanaurl);
        if(isUp){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }
    @GetMapping("/alertmanager_health")
    public ResponseEntity<Boolean>alertmanagerHealthCheck() {
        System.out.println("OK");
        boolean isUp=this.checkHealth.isServiceUp(alertmanager);
        System.out.println("lup"+isUp);
        if(isUp){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }
        @GetMapping("/restart_prometheus")
    public ResponseEntity<Boolean>restartPrometheus() {
        System.out.println("OK");
        boolean isRestarted=this.checkHealth.reloadPrometheusAlertManagerServer(prometheusurl);
        System.out.println("lup"+isRestarted);
        if(isRestarted){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    @GetMapping("/restart_alertmanager")
    public ResponseEntity<Boolean>restartAlertManager() {
        System.out.println("OK");
        boolean isRestarted=this.checkHealth.reloadPrometheusAlertManagerServer(alertmanager);
        System.out.println("lup"+isRestarted);
        if(isRestarted){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    @GetMapping("/restart_prometheus_container")
    public ResponseEntity<Boolean>restartPrometheusContainer() throws IOException {
        System.out.println("OK");
        boolean isRestarted=this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);
        System.out.println("lup"+isRestarted);
        if(isRestarted){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }

    @GetMapping("/restart_alertmanager_container")
    public ResponseEntity<Boolean>restartAlertManagerContainer() throws IOException {
        System.out.println("OK");
        boolean isRestarted=this.prometheusAlertService.executeShellCommand(alertManagerRestartCommand);
        System.out.println("lup"+isRestarted);
        if(isRestarted){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);

    }
/*
    @GetMapping("/restart_container")
    public void restartDockerContainer(@RequestParam(value="containername") String containerName) throws IOException {
        String containerId = this.getContainerIdByName(containerName);
        System.out.println(containerId);

        String commandEntete=" curl --unix-socket /var/run/docker.sock -X POST ";
        // String url="http://172.18.2.202:2375/containers/"+containerId+"/restart";
        String url="http://localhost:2375/containers/"+containerId+"/restart";

        String command=commandEntete+url;
        try {
            Process process = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }*/

}

