package com.expo.prometheus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
@Service
public class PrometheusConfigFileGenerator {




    private static final String ALERT_FILE_NAME = "alert.yml";
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";
    private static final String PROMETHEUS_CONFIG_FILE="prometheus.yml";

    public PrometheusConfigFileGenerator(PrometheusAlertService prometheusAlertService) {
        this.prometheusAlertService = prometheusAlertService;
    }

    private PrometheusAlertService prometheusAlertService;



    String theLocalFile=RESOURCES_DIRECTORY+PROMETHEUS_CONFIG_FILE;
    @Value("${alertmanager.config.path}")
    private String alertManagerConfigPath;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;
    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;


    public void generateConfigFile() {
        // Define the alertmanager configuration content
        String configContent = "global:\n" +
                "  scrape_interval: 15s\n" +
                "rule_files:\n" +
                "  - "+ALERT_FILE_NAME+"\n" +
                "alerting:\n" +
                "  alertmanagers:\n" +
                "    - static_configs:\n"+
                "      - targets: ['localhost:9093']\n" +
                "scrape_configs:\n" ;


        // Specify the path and name for the configuration file
        String filePath = RESOURCES_DIRECTORY+PROMETHEUS_CONFIG_FILE;

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(configContent);
            writer.close();
            System.out.println("Alertmanager configuration file generated successfully.");
        } catch (IOException e) {
            System.err.println("Failed to write the configuration file: " + e.getMessage());
        }
    }



    public void addAppToFile(String jobName, String metricsPath, String ipaddr, String port ) {
        String receiver=generateAppConfig(jobName,metricsPath,ipaddr,port);
        try {
            Path prometheusFilePath = Path.of(RESOURCES_DIRECTORY, PROMETHEUS_CONFIG_FILE);

            if (Files.exists(prometheusFilePath)) {
                Files.writeString(prometheusFilePath, receiver + "\n", StandardOpenOption.APPEND);
                System.out.println("Target added to the file successfully.");
                System.out.println(theLocalFile);
                this.prometheusAlertService.pushRuleFile(theLocalFile);

                // Execute the command to restart or reload Prometheus
                this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);

            }
            else {
                //   System.err.println("Rule file does not exist. Please generate the rule file first.");
                System.err.println("Rule File generated");

                generateConfigFile();
                Files.writeString(prometheusFilePath, receiver + "\n", StandardOpenOption.APPEND);


                   this.prometheusAlertService.pushRuleFile(theLocalFile);

                // Execute the command to restart or reload Prometheus
                  this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);

            }
        } catch (IOException e) {
            System.err.println("Failed to add the rule to the file: " + e.getMessage());
        }
    }
    public static String generateAppConfig(String jobName, String metricsPath, String ipaddr, String port ) {
        String receiverConfig =
                "  - job_name: '" + jobName + "'\n" +
                        "    metrics_path: " + metricsPath + "\n" +
                        "    static_configs :\n" +
                        "    - targets: \n" +
                        "      - '" + ipaddr +":"+port + "'\n" ;

        return receiverConfig;

    }



}
