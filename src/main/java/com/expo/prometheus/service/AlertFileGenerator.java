package com.expo.prometheus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class AlertFileGenerator {

    private static final String ALERT_FILE_NAME = "alert.yml";
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";


    String theLocalRulesFile=RESOURCES_DIRECTORY+ALERT_FILE_NAME;
    @Value("${alertmanager.config.path}")
    private String alertManagerConfigPath;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;


    public void generateConfigFile() {
        // Define the alertmanager configuration content
        String configContent = "global:\n" +
                "  resolve_timeout: 5m\n" +
                "route:\n" +
                "  group_wait: 30s\n" +
                "  group_interval: 5m\n" +
                "  repeat_interval: 12h\n" +
                "receivers:\n" ;


        // Specify the path and name for the configuration file
        String filePath = RESOURCES_DIRECTORY+ALERT_FILE_NAME;

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(configContent);
            writer.close();
            System.out.println("Alertmanager configuration file generated successfully.");
        } catch (IOException e) {
            System.err.println("Failed to write the configuration file: " + e.getMessage());
        }
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


    public void addReceiverToFile(String receiverName, String receiverEmail, String senderEmail, String smarthost, String username, String password) {
        String receiver=generateReceiverConfig(receiverName,receiverEmail,senderEmail,smarthost,username,password);
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                Files.writeString(alertFilePath, receiver + "\n", StandardOpenOption.APPEND);
                System.out.println("Rule added to the file successfully.");
            } else {
                //   System.err.println("Rule file does not exist. Please generate the rule file first.");
                System.err.println("Rule File generated");

                generateConfigFile();
                Files.writeString(alertFilePath, receiver + "\n", StandardOpenOption.APPEND);


            //    this.prometheusAlertService.pushRuleFile(theLocalRulesFile);

                // Execute the command to restart or reload Prometheus
             //   this.prometheusAlertService.executeShellCommand(prometheusRestartCommand);

            }
        } catch (IOException e) {
            System.err.println("Failed to add the rule to the file: " + e.getMessage());
        }
    }
    public static String generateReceiverConfig(String receiverName, String receiverEmail, String senderEmail, String smarthost, String username, String password) {
        String receiverConfig =
                "  - name: '" + receiverName + "'\n" +
                        "    email_configs:\n" +
                        "      - to: '" + receiverEmail + "'\n" +
                        "        from: '" + senderEmail + "'\n" +
                        "        smarthost: '" + smarthost + "'\n" +
                        "        smtp_auth_username: '" + username + "'\n" +
                        "        smtp_auth_password: '" + password + "'\n";

        return receiverConfig;

        // addReceiverToReceiversList(,receiverConfig);
    }




}
