package com.expo.prometheus.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;

@Service
@CrossOrigin(origins = "*")

public class PrometheusAlertService {


    @Value("${prometheus.config.path}")
    private String prometheusConfigPath;

    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;
    @Value("${alertmanager.config.path}")
    private String alertManagerConfigPath;


        public void pushRuleFile(String ruleFilePath,String type ) throws IOException {
            if(type=="rule"){
                // Copy the rule file to the Prometheus configuration directory
                String copyCommand = "cp " + ruleFilePath + " " +prometheusConfigPath ;

                executeShellCommand(copyCommand);
                System.out.println("File copied to Prometheus configuration directory.");

                // Restart or reload the Prometheus server
                executeShellCommand(prometheusRestartCommand);
                System.out.println("Prometheus server restarted.");

            }
            if (type=="alert")
            {
                String copyCommand = "cp " + ruleFilePath + " " + alertManagerConfigPath;
                executeShellCommand(copyCommand);
                System.out.println("File copied to Alert Manager configuration directory.");

                // Restart or reload the Prometheus server
                executeShellCommand(alertManagerRestartCommand);
                System.out.println("Alert Manager server restarted.");
            }
            else{

                String copyCommand = "cp " + ruleFilePath + " " + type;
                executeShellCommand(copyCommand);
                System.out.println("File copied to Prometheus configuration directory.");

                // Restart or reload the Prometheus server
                executeShellCommand(prometheusRestartCommand);
                System.out.println("Prometheus server restarted.");
            }
        }

        public void executeShellCommand(String command) throws IOException {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Failed to execute shell command: " + command);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
}
