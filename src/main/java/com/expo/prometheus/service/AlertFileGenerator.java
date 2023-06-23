package com.expo.prometheus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    public void addReceiverToFile(String receiverName, List<String> receiverEmails, String senderEmail, String smarthost, String username, String password) {
        String receiver = generateReceiverConfig(receiverName, receiverEmails, senderEmail, smarthost, username, password);
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                Files.writeString(alertFilePath, receiver + "\n", StandardOpenOption.APPEND);
                System.out.println("Receiver added to the file successfully.");
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                addReceiverToFile(receiverName, receiverEmails, senderEmail, smarthost, username, password);
            }
        } catch (IOException e) {
            System.err.println("Failed to add the receiver to the file: " + e.getMessage());
        }
    }
    public static String generateReceiverConfig(String receiverName, List<String> receiverEmails, String senderEmail, String smarthost, String username, String password) {
        String Emails="";
        StringBuilder emailConfigs = new StringBuilder();
        for (String receiverEmail : receiverEmails) {
            System.out.println("receiverEmailback"+receiverEmail);
            Emails = Emails +"'" +receiverEmail;
            System.out.println("Emails"+Emails);

            if (receiverEmail != receiverEmails.get(receiverEmails.size() - 1)) {
                Emails = Emails + "'"+",";
            }
        }
        emailConfigs.append("      - to: ").append(Emails);

        String receiverConfig =
                "  - name: '" + receiverName + "'\n" +
                        "    email_configs:\n" +
                        emailConfigs.toString() +"'\n"+
                        "        from: '" + senderEmail + "'\n" +
                        "        smarthost: '" + smarthost + "'\n" +
                        "        smtp_auth_username: '" + username + "'\n" +
                        "        smtp_auth_password: '" + password + "'\n";

        return receiverConfig;
    }

    private void restartAlertManager() {
        try {
            // Execute the command to restart or reload the Alertmanager service
            Runtime.getRuntime().exec(alertManagerRestartCommand);
        } catch (IOException e) {
            System.err.println("Failed to restart the Alertmanager service: " + e.getMessage());
        }
    }
    public void addEmailToReceiver(String receiverName, String receiverEmail) {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                String existingConfigContent = Files.readString(alertFilePath);
                String updatedConfigContent = addEmailToReceiverConfig(existingConfigContent, receiverName, receiverEmail);

                Files.writeString(alertFilePath, updatedConfigContent);
                System.out.println("Email added to the receiver successfully.");
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                addEmailToReceiver(receiverName, receiverEmail);
            }
        } catch (IOException e) {
            System.err.println("Failed to add email to the receiver: " + e.getMessage());
        }
    }

    private String addEmailToReceiverConfig(String configContent, String receiverName, String additionalEmail) {
        // Find the receiver configuration for the given receiver name
        String receiverConfigStart = "  - name: '" + receiverName + "'";
        int receiverConfigIndex = configContent.indexOf(receiverConfigStart);
        if (receiverConfigIndex == -1) {
            System.err.println("Receiver configuration not found for the given receiver name.");
            return configContent;
        }

        // Find the end index of the receiver configuration
        int receiverConfigEndIndex = configContent.indexOf('\n', receiverConfigIndex);
        if (receiverConfigEndIndex == -1) {
            System.err.println("Failed to update receiver configuration.");
            return configContent;
        }

        // Extract the existing receiver configuration
        String existingReceiverConfig = configContent.substring(receiverConfigIndex, receiverConfigEndIndex);

        // Add the additional email to the existing receiver configuration
        String updatedReceiverConfig = existingReceiverConfig + "\n      - to: '" + additionalEmail + "'";

        // Replace the existing receiver configuration with the updated one
        String updatedConfigContent = configContent.replace(existingReceiverConfig, updatedReceiverConfig);

        return updatedConfigContent;
    }

    public void updateSMTPCredentials(String receiverName, String username, String password) {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                String existingConfigContent = Files.readString(alertFilePath);
                String updatedConfigContent = updateSMTPCredentialsConfig(existingConfigContent, receiverName, username, password);

                Files.writeString(alertFilePath, updatedConfigContent);
                System.out.println("SMTP credentials updated successfully.");
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                updateSMTPCredentials(receiverName, username, password);
            }
        } catch (IOException e) {
            System.err.println("Failed to update SMTP credentials: " + e.getMessage());
        }
    }

    private String updateSMTPCredentialsConfig(String configContent, String receiverName, String username, String password) {
        // Find the receiver configuration for the given receiver name
        String receiverConfigStart = "  - name: '" + receiverName + "'";
        int receiverConfigIndex = configContent.indexOf(receiverConfigStart);
        if (receiverConfigIndex == -1) {
            System.err.println("Receiver configuration not found for the given receiver name.");
            return configContent;
        }

        // Find the end index of the receiver configuration
        int receiverConfigEndIndex = configContent.indexOf('\n', receiverConfigIndex);
        if (receiverConfigEndIndex == -1) {
            System.err.println("Failed to update receiver configuration.");
            return configContent;
        }

        // Extract the existing receiver configuration
        String existingReceiverConfig = configContent.substring(receiverConfigIndex, receiverConfigEndIndex);

        // Replace the existing SMTP credentials with the updated ones
        String updatedReceiverConfig = existingReceiverConfig
                .replaceFirst("smtp_auth_username: '[^']+'", "smtp_auth_username: '" + username + "'")
                .replaceFirst("smtp_auth_password: '[^']+'", "smtp_auth_password: '" + password + "'");

        // Replace the existing receiver configuration with the updated one
        String updatedConfigContent = configContent.replace(existingReceiverConfig, updatedReceiverConfig);

        return updatedConfigContent;
    }
    public void updateReceiverName(String currentReceiverName, String newReceiverName) {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                String existingConfigContent = Files.readString(alertFilePath);
                String updatedConfigContent = updateReceiverNameConfig(existingConfigContent, currentReceiverName, newReceiverName);

                Files.writeString(alertFilePath, updatedConfigContent);
                System.out.println("Receiver name updated successfully.");
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                updateReceiverName(currentReceiverName, newReceiverName);
            }
        } catch (IOException e) {
            System.err.println("Failed to update receiver name: " + e.getMessage());
        }
    }

    private String updateReceiverNameConfig(String configContent, String currentReceiverName, String newReceiverName) {
        // Find the receiver configuration for the current receiver name
        String receiverConfigStart = "  - name: '" + currentReceiverName + "'";
        int receiverConfigIndex = configContent.indexOf(receiverConfigStart);
        if (receiverConfigIndex == -1) {
            System.err.println("Receiver configuration not found for the given receiver name.");
            return configContent;
        }

        // Find the end index of the receiver configuration
        int receiverConfigEndIndex = configContent.indexOf('\n', receiverConfigIndex);
        if (receiverConfigEndIndex == -1) {
            System.err.println("Failed to update receiver configuration.");
            return configContent;
        }

        // Extract the existing receiver configuration
        String existingReceiverConfig = configContent.substring(receiverConfigIndex, receiverConfigEndIndex);

        // Replace the current receiver name with the new receiver name
        String updatedReceiverConfig = existingReceiverConfig.replace(currentReceiverName, newReceiverName);

        // Replace the existing receiver configuration with the updated one
        String updatedConfigContent = configContent.replace(existingReceiverConfig, updatedReceiverConfig);

        return updatedConfigContent;
    }
    public List<String> getAlertsByRule(String ruleName) {
        List<String> alertNames = new ArrayList<>();

        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                String configContent = Files.readString(alertFilePath);

                // Find all rule expressions
                Pattern pattern = Pattern.compile("expr: (.+?)\\n");
                Matcher matcher = pattern.matcher(configContent);

                while (matcher.find()) {
                    String ruleExpression = matcher.group(1);

                    // Extract the alert name from the rule expression
                    String alertName = extractAlertName(ruleExpression);

                    // Check if the rule name matches the provided ruleName parameter
                    if (ruleName.equals(alertName)) {
                        alertNames.add(alertName);
                    }
                }

                if (alertNames.isEmpty()) {
                    System.out.println("No alerts found for the given rule: " + ruleName);
                }
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                alertNames = getAlertsByRule(ruleName);
            }
        } catch (IOException e) {
            System.err.println("Failed to retrieve alerts by rule: " + e.getMessage());
        }

        return alertNames;
    }

    private String extractAlertName(String ruleExpression) {
        // Extract the alert name from the rule expression
        String[] parts = ruleExpression.split("\\{");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return "";
    }



}
