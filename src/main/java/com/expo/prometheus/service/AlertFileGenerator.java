package com.expo.prometheus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AlertFileGenerator {

    private static final String ALERT_FILE_NAME = "alert.yml";
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";


    @Value("${prometheus.server.url}")
    private String prometheusServerurl;
    @Value("${prometheus.config.path}")
    private String prometheusConfigPath;
    String theLocalRulesFile=RESOURCES_DIRECTORY+ALERT_FILE_NAME;
    private PrometheusAlertService prometheusAlertService;
    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;
    public AlertFileGenerator(PrometheusAlertService prometheusAlertService) {
        this.prometheusAlertService = prometheusAlertService;
    }

    public void generateConfigFile(String defaultReceiver, String alertName, String receiver) {
        // Define the alertmanager configuration content
        String configContent = "global:\n" +
                "  resolve_timeout: 5m\n" +
                "route:\n" +
                "  group_wait: 30s\n" +
                "  group_interval: 5m\n" +
                "  repeat_interval: 12h\n" +
                "  receiver: '" + defaultReceiver + "'\n" +
                "receivers:\n" +
                "  - name: '" + defaultReceiver + "'\n" +
                "    email_configs:\n" +
                "      - to: 'default@example.com'\n" +
                "        from: 'alertmanager@example.com'\n" +
                "        smarthost: 'smtp.example.com'\n" +
                "        smtp_auth_username: 'username'\n" +
                "        smtp_auth_password: 'password'\n" +
                "route:\n" +
                "  routes:\n" +
                "    - receiver: '" + receiver + "'\n" +
                "      group_wait: 10s\n" +
                "      match:\n" +
                "        alertname: '" + alertName + "'\n";

        // Specify the path and name for the configuration file
        String filePath = "alertmanager.yml";

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(configContent);
            writer.close();
            System.out.println("Alertmanager configuration file generated successfully.");
        } catch (IOException e) {
            System.err.println("Failed to write the configuration file: " + e.getMessage());
        }
    }

    public static String generateReceiverConfig(String receiverName, String receiverEmail, String senderEmail, String smarthost, String username, String password) {
        String receiverConfig = "  - name: '" + receiverName + "'\n" +
                "    email_configs:\n" +
                "      - to: '" + receiverEmail + "'\n" +
                "        from: '" + senderEmail + "'\n" +
                "        smarthost: '" + smarthost + "'\n" +
                "        smtp_auth_username: '" + username + "'\n" +
                "        smtp_auth_password: '" + password + "'\n";

        return receiverConfig;
    }
    public static String addReceiverToReceiversList(String currentConfigContent, String receiverConfig) {
        // Find the index where the receivers list ends
        int endIndex = currentConfigContent.indexOf("receivers:\n") + 11;

        // Insert the new receiver config at the end of the receivers list
        String updatedConfigContent = currentConfigContent.substring(0, endIndex) +
                receiverConfig + currentConfigContent.substring(endIndex);

        return updatedConfigContent;
    }
    /*public static void generateConfigFile(String configContent, String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(configContent);
            writer.close();
            System.out.println("AlertManager configuration file generated successfully.");
        } catch (IOException e) {
            System.err.println("Failed to write the configuration file: " + e.getMessage());
        }
    }*/


}
