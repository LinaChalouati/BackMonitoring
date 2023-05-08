package com.expo.healthcheck.controller;

import com.expo.healthcheck.model.AppHealthCheckResult;
import com.expo.healthcheck.service.AppHealthCheckService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.Socket;

@RestController
@CrossOrigin("*")
public class AppHealthCheck {
 private AppHealthCheckService checkHealth;

    public AppHealthCheck(AppHealthCheckService checkHealth) {
        this.checkHealth = checkHealth;
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


    }

