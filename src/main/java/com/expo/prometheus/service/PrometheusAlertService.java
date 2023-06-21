package com.expo.prometheus.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PrometheusAlertService {


    @Value("${prometheus.config.path}")
    private String prometheusConfigPath;

    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;




        public void pushRuleFile(String ruleFilePath) throws IOException {
            // Copy the rule file to the Prometheus configuration directory
            String copyCommand = "cp " + ruleFilePath + " " + prometheusConfigPath;
            executeShellCommand(copyCommand);
            System.out.println("File copied to Prometheus configuration directory.");

            // Restart or reload the Prometheus server
            executeShellCommand(prometheusRestartCommand);
            System.out.println("Prometheus server restarted.");
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
