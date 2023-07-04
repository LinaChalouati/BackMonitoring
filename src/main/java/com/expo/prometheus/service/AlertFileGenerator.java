package com.expo.prometheus.service;

import com.expo.prometheus.model.AlertEmailInfo;
import com.expo.prometheus.model.MailNotifInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class AlertFileGenerator {

    private static final String ALERT_FILE_NAME = "alert.yml";
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";
    @Value("${smtp.smarthost}")
    private String smarthost;
    @Value("${smtp.from}")
    private String smtpfrom;
    @Value("${smtp.auth_username}")
    private String auth_username;
    @Value("${smtp.auth_password}")
    private String auth_password;

    String theLocalRulesFile=RESOURCES_DIRECTORY+ALERT_FILE_NAME;
    @Value("${alertmanager.config.path}")
    private String alertManagerConfigPath;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;
    private PrometheusAlertService prometheusAlertService;

    public AlertFileGenerator(PrometheusAlertService prometheusAlertService) {
        this.prometheusAlertService = prometheusAlertService;
    }

    public void generateConfigFile() {
        // Define the alertmanager configuration content
        String configContent = "global:\n" +
                "  resolve_timeout: 5m\n" +
                "  smtp_smarthost: "+smarthost+"\n" +
                "  smtp_from: "+smtpfrom+"\n" +
                "  smtp_auth_username: "+auth_username+"\n" +
                "  smtp_auth_password: "+auth_password+"\n" +

                "route:\n" +
                "  group_wait: 30s\n" +
                "  group_interval: 5m\n" +
                "  repeat_interval: 12h\n" +
                "  receiver: default-receiver\n" +
                "  group_by: ['alertname']\n" +
                "  routes: \n" +

                "receivers:\n" ;


        // Specify the path and name for the configuration file
        String filePath = RESOURCES_DIRECTORY+ALERT_FILE_NAME;

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(configContent);
            writer.close();
            System.out.println("Alertmanager configuration file generated successfully.");
            addReceiverToFile("default-receiver", Collections.singletonList("default@example.com"));
         //   this.prometheusAlertService.pushRuleFile(theLocalRulesFile,alertManagerConfigPath);

            // Execute the command to restart or reload Prometheus
            //this.prometheusAlertService.executeShellCommand(alertManagerRestartCommand);
        } catch (IOException e) {
            System.err.println("Failed to write the configuration file: " + e.getMessage());
        }
    }


    public void addRoute(String alertName, String receiverName, String instance, List<String> receiverEmails) throws IOException {

        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (!Files.exists(alertFilePath)) {
                System.err.println("Alert file does not exist. Generating the file first.");
                generateConfigFile();
               // addRoute(alertName, receiverName, instance, receiverEmails);

            }

            Map<String, Object> config = loadYamlConfig(alertFilePath);

            if (config == null) {
                config = new LinkedHashMap<>();
            }

            Map<String, Object> routeSection = (Map<String, Object>) config.get("route");

            if (routeSection == null) {
                routeSection = new LinkedHashMap<>();
                config.put("route", routeSection);
            }

            List<Map<String, Object>> routes = (List<Map<String, Object>>) routeSection.get("routes");

            if (routes == null) {
                routes = new ArrayList<>();
                routeSection.put("routes", routes);
            }

            Map<String, Object> newRoute = new LinkedHashMap<>();
            Map<String, Object> matchLabels = new LinkedHashMap<>();
            matchLabels.put("alertname", alertName);
            matchLabels.put("instance", instance);
            newRoute.put("match", matchLabels);
            newRoute.put("receiver", receiverName);
            newRoute.put("continue", true);
            routes.add(newRoute);

            writeYamlConfig(config, alertFilePath);

            System.out.println("Route added to the file successfully.");
            addReceiverToFile(receiverName, receiverEmails);
        } catch (IOException e) {
            throw new IOException("Failed to add the route to the file: " + e.getMessage(), e);
        }
    }

    public void addReceiverToFile(String receiverName, List<String> receiverEmails) throws IOException {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (!Files.exists(alertFilePath)) {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                return;
            }

            Map<String, Object> config = loadYamlConfig(alertFilePath);

            if (config == null) {
                config = new LinkedHashMap<>();
            }

            List<Map<String, Object>> receivers = (List<Map<String, Object>>) config.get("receivers");

            if (receivers == null) {
                receivers = new ArrayList<>();
                config.put("receivers", receivers);
            }

            Map<String, Object> newReceiver = generateReceiverConfig(receiverName, receiverEmails);
            receivers.add(newReceiver);

            writeYamlConfig(config, alertFilePath);

            System.out.println("Receiver added to the file successfully.");
        } catch (IOException e) {
            throw new IOException("Failed to add the receiver to the file: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> generateReceiverConfig(String receiverName, List<String> receiverEmails) {
        Map<String, Object> receiverConfig = new LinkedHashMap<>();
        receiverConfig.put("name", receiverName);

        List<Map<String, Object>> emailConfigs = new ArrayList<>();
        for (String receiverEmail : receiverEmails) {
            Map<String, Object> emailConfig = new LinkedHashMap<>();
            emailConfig.put("to", receiverEmail);
            emailConfigs.add(emailConfig);
        }
        receiverConfig.put("email_configs", emailConfigs);

        return receiverConfig;
    }

    public void updateReceiverName(String oldReceiverName, String newReceiverName) throws IOException {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (!Files.exists(alertFilePath)) {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                return;
            }

            Map<String, Object> config = loadYamlConfig(alertFilePath);

            if (config == null) {
                System.err.println("Invalid configuration file.");
                return;
            }

            Map<String, Object> routesSection = (Map<String, Object>) config.get("route");

            if (routesSection != null) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) routesSection.get("routes");

                if (routes != null) {
                    for (Map<String, Object> route : routes) {
                        String receiverName = (String) route.get("receiver");
                        if (receiverName.equals(oldReceiverName)) {
                            route.put("receiver", newReceiverName);
                        }
                    }
                }
            }

            List<Map<String, Object>> receivers = (List<Map<String, Object>>) config.get("receivers");

            if (receivers != null) {
                for (Map<String, Object> receiver : receivers) {
                    String receiverName = (String) receiver.get("name");
                    if (receiverName.equals(oldReceiverName)) {
                        receiver.put("name", newReceiverName);
                    }
                }
            }

            writeYamlConfig(config, alertFilePath);

            System.out.println("Receiver name updated successfully.");
        } catch (IOException e) {
            throw new IOException("Failed to update the receiver name: " + e.getMessage(), e);
        }
    }

    public boolean addEmailsToReceiver(String receiverName, List<String> emails) throws IOException {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (!Files.exists(alertFilePath)) {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                return false;
            }

            Map<String, Object> config = loadYamlConfig(alertFilePath);

            if (config != null) {
                List<Map<String, Object>> receivers = (List<Map<String, Object>>) config.get("receivers");

                if (receivers != null) {
                    for (Map<String, Object> receiver : receivers) {
                        String name = (String) receiver.get("name");
                        if (name.equals(receiverName)) {
                            List<Map<String, Object>> emailConfigs = (List<Map<String, Object>>) receiver.get("email_configs");
                            if (emailConfigs == null) {
                                emailConfigs = new ArrayList<>();
                                receiver.put("email_configs", emailConfigs);
                            }

                            for (String email : emails) {
                                Map<String, Object> emailMap = new HashMap<>();
                                emailMap.put("to", email);
                                emailConfigs.add(emailMap);
                            }

                            writeYamlConfig(config, alertFilePath);

                            System.out.println("Emails added to the receiver successfully.");
                            return true;
                        }
                    }
                }

                System.err.println("Receiver not found in the configuration.");
            } else {
                System.err.println("Invalid configuration file.");
            }
        } catch (IOException e) {
            throw new IOException("Failed to add emails to the receiver: " + e.getMessage(), e);
        }
        return false;
    }


    private void writeYamlConfig(Map<String, Object> config, Path filePath) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            yaml.dump(config, writer);
        }
    }

    private Map<String, Object> loadYamlConfig(Path filePath) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return yaml.load(inputStream);
        }
    }


    public boolean disableEnableAlert(String alertName, String instance) throws IOException {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                // Load the configuration file as a YAML object
                Yaml yaml = new Yaml();

                // Parse the configuration file
                Map<String, Object> config = yaml.load(Files.newInputStream(alertFilePath));

                if (config != null) {
                    // Get the 'route' section from the configuration
                    Map<String, Object> routeSection = (Map<String, Object>) config.get("route");

                    if (routeSection != null) {
                        List<Map<String, Object>> routes = (List<Map<String, Object>>) routeSection.get("routes");

                        if (routes != null) {
                            // Search for the alert with the specified name and instance
                            for (Map<String, Object> route : routes) {
                                String routeAlertName = (String) ((Map<String, Object>) route.get("match")).get("alertname");
                                String routeInstance = (String) ((Map<String, Object>) route.get("match")).get("instance");

                                // Check if the routeAlertName and routeInstance are not null before calling equals
                                if (routeAlertName != null && routeInstance != null &&
                                        routeAlertName.equals(alertName) && routeInstance.equals(instance)) {
                                    // Reverse the 'continue' flag value
                                    route.put("continue", !((boolean) route.get("continue")));
                                }
                            }
                        }
                    }

                    // Write the updated configuration back to the file
                    FileWriter writer = new FileWriter(alertFilePath.toFile());
                    yaml.dump(config, writer);
                    writer.close();

                    System.out.println("Alert disabled successfully.");
                    return true;
                } else {
                    System.err.println("Invalid configuration file.");
                }
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
            }
        } catch (IOException e) {
            System.err.println("Failed to disable the alert: " + e.getMessage());
        }
        return false;
    }
    // ok mrigl

    public boolean deleteAlert(String alertName, String instance) throws IOException {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);
            String receiverNameToDelete = "";

            if (Files.exists(alertFilePath)) {
                // Load the configuration file as a YAML object
                Yaml yaml = new Yaml();

                // Parse the configuration file
                Map<String, Object> config = yaml.load(Files.newInputStream(alertFilePath));

                if (config != null) {
                    // Get the 'route' section from the configuration
                    Map<String, Object> routeSection = (Map<String, Object>) config.get("route");
                    List<Map<String, Object>> receiversSection = (List<Map<String, Object>>) config.get("receivers");

                    if (routeSection != null) {
                        List<Map<String, Object>> routes = (List<Map<String, Object>>) routeSection.get("routes");

                        if (routes != null) {
                            // Search for the alert with the specified name and instance
                            Iterator<Map<String, Object>> iterator = routes.iterator();
                            while (iterator.hasNext()) {
                                Map<String, Object> route = iterator.next();
                                Map<String, Object> match = (Map<String, Object>) route.get("match");
                                String routeAlertName = (String) match.get("alertname");
                                String routeInstance = (String) match.get("instance");
                                receiverNameToDelete = (String) route.get("receiver");

                                // Check if the routeAlertName and routeInstance are not null before calling equals
                                if (routeAlertName != null && routeInstance != null &&
                                        routeAlertName.equals(alertName) && routeInstance.equals(instance)) {
                                    iterator.remove(); // Remove the alert from the list of routes
                                }
                            }
                        }
                    }

                    // Remove the alert from the receivers as well
                    if (receiversSection != null) {
                        Iterator<Map<String, Object>> receiverIterator = receiversSection.iterator();
                        while (receiverIterator.hasNext()) {
                            Map<String, Object> receiver = receiverIterator.next();
                            String receiverName = (String) receiver.get("name");

                            // Check if the receiverName is not null before calling equals
                            if (receiverName != null && receiverName.equals(receiverNameToDelete)) {
                                receiverIterator.remove(); // Remove the receiver from the list
                            }
                        }
                    }

                    // Write the updated configuration back to the file
                    FileWriter writer = new FileWriter(alertFilePath.toFile());
                    yaml.dump(config, writer);
                    writer.close();

                    System.out.println("Alert deleted successfully.");
                    return true;
                } else {
                    System.err.println("Invalid configuration file.");
                }
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                deleteAlert(alertName, instance);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete the alert: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteEmailFromReceiver(String receiverName, String receiverEmail) throws IOException {
        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                // Load the configuration file as a YAML object
                Yaml yaml = new Yaml();

                // Parse the configuration file
                Map<String, Object> config = yaml.load(Files.newInputStream(alertFilePath));

                if (config != null) {
                    // Get the 'receivers' section from the configuration
                    List<Map<String, Object>> receiversSection = (List<Map<String, Object>>) config.get("receivers");

                    if (receiversSection != null) {
                        // Iterate over the receivers and find the receiver by name
                        for (Map<String, Object> receiver : receiversSection) {
                            String currentReceiverName = (String) receiver.get("name");
                            if (currentReceiverName != null && currentReceiverName.equals(receiverName)) {
                                List<Map<String, String>> emailConfigs = (List<Map<String, String>>) receiver.get("email_configs");
                                if (emailConfigs != null) {
                                    Iterator<Map<String, String>> emailIterator = emailConfigs.iterator();
                                    while (emailIterator.hasNext()) {
                                        Map<String, String> emailConfig = emailIterator.next();
                                        String currentEmail = emailConfig.get("to");

                                        // Check if the currentEmail is not null before calling equals
                                        if (currentEmail != null && currentEmail.equals(receiverEmail)) {
                                            emailIterator.remove(); // Remove the email from the receiver's list
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Write the updated configuration back to the file
                    FileWriter writer = new FileWriter(alertFilePath.toFile());
                    yaml.dump(config, writer);
                    writer.close();

                    System.out.println("Email deleted successfully from the receiver.");
                    return true;
                } else {
                    System.err.println("Invalid configuration file.");
                }
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
            }
        } catch (IOException e) {
            System.err.println("Failed to delete the email from the receiver: " + e.getMessage());
        }
        return false;
    }
//aandi mochkla lena nheb nzid l alert name w instance both

    public MailNotifInfo getEmailsByAlert(String alertName, String instance) throws IOException {
        List<String> emails = new ArrayList<>();
        boolean state = false; // Initialize the state variable
        String receiver = null; // Initialize the receiver variable

        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                // Load the configuration file as a YAML object
                Yaml yaml = new Yaml();

                // Parse the configuration file
                Map<String, Object> config = yaml.load(Files.newInputStream(alertFilePath));

                if (config != null) {
                    // Get the 'route' section from the configuration
                    Map<String, Object> routeSection = (Map<String, Object>) config.get("route");
                    List<MailNotifInfo> receivers = parseReceivers(config.get("receivers"), instance, state);
                    if (routeSection != null && receivers != null) {
                        List<Map<String, Object>> routes = (List<Map<String, Object>>) routeSection.get("routes");

                        if (routes != null) {
                            // Search for the alert with the specified name and instance
                            for (Map<String, Object> route : routes) {
                                Map<String, Object> match = (Map<String, Object>) route.get("match");
                                String routeAlertName = (String) match.get("alertname");
                                String routeInstance = (String) match.get("instance");

                                // Check if the routeAlertName and routeInstance are not null before calling equals
                                if (routeAlertName != null && routeInstance != null &&
                                        routeAlertName.equals(alertName) && routeInstance.equals(instance)) {
                                    receiver = (String) route.get("receiver"); // Get the receiver name
                                    emails.addAll(getEmailsByReceiverName(receiver, receivers));
                                    state = (boolean) route.get("continue"); // Update the state variable
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    System.err.println("Invalid configuration file.");
                }
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                return getEmailsByAlert(alertName, instance);
            }
        } catch (IOException e) {
            System.err.println("Failed to retrieve emails for the alert: " + e.getMessage());
        }

        return new MailNotifInfo(alertName, instance, emails, state, receiver);
    }

    private List<MailNotifInfo> parseReceivers(Object receiversObject, String instance, boolean state) {
        List<MailNotifInfo> receivers = new ArrayList<>();

        if (receiversObject instanceof List) {
            List<Map<String, Object>> receiversList = (List<Map<String, Object>>) receiversObject;

            for (Map<String, Object> receiverMap : receiversList) {
                String name = (String) receiverMap.get("name");
                List<String> emails = getEmailsFromReceiverMap(receiverMap);
                String receiver = name; // Set the receiver value as the name
                receivers.add(new MailNotifInfo(name, instance, emails, state, receiver)); // Pass the state and receiver parameters
            }
        }

        return receivers;
    }



    private List<String> getEmailsFromReceiverMap(Map<String, Object> receiverMap) {
        List<String> emails = new ArrayList<>();

        List<Map<String, String>> emailConfigs = (List<Map<String, String>>) receiverMap.get("email_configs");

        if (emailConfigs != null) {
            for (Map<String, String> emailConfig : emailConfigs) {
                String email = emailConfig.get("to");
                if (email != null) {
                    emails.add(email);
                }
            }
        }

        return emails;
    }

    private List<String> getEmailsByReceiverName(String receiverName, List<MailNotifInfo> receivers) {
        List<String> emails = new ArrayList<>();

        for (MailNotifInfo receiver : receivers) {
            if (receiver.getAlertname().equals(receiverName)) {
                emails.addAll(receiver.getEmails());
                break;
            }
        }

        return emails;
    }

    public List<AlertEmailInfo> getAlertsAndEmailsByInstance(String instance) throws IOException {
        List<AlertEmailInfo> alertEmailInfos = new ArrayList<>();

        try {
            Path alertFilePath = Path.of(RESOURCES_DIRECTORY, ALERT_FILE_NAME);

            if (Files.exists(alertFilePath)) {
                // Load the configuration file as a YAML object
                Yaml yaml = new Yaml();

                // Parse the configuration file
                Map<String, Object> config = yaml.load(Files.newInputStream(alertFilePath));

                if (config != null) {
                    // Get the 'route' section from the configuration
                    Map<String, Object> routeSection = (Map<String, Object>) config.get("route");

                    if (routeSection != null) {
                        List<Map<String, Object>> routes = (List<Map<String, Object>>) routeSection.get("routes");

                        if (routes != null) {
                            for (Map<String, Object> route : routes) {
                                Map<String, Object> match = (Map<String, Object>) route.get("match");
                                String routeInstance = (String) match.get("instance");
                                String alertName = (String) match.get("alertname");

                                if (routeInstance != null && routeInstance.equals(instance)) {
                                    String receiver = (String) route.get("receiver"); // Get the receiver name

                                    List<String> emails = getEmailsByReceiverName(receiver, config);

                                    // Create AlertEmailInfo object and add it to the list
                                    AlertEmailInfo alertEmailInfo = new AlertEmailInfo(alertName, instance, receiver, emails);
                                    alertEmailInfos.add(alertEmailInfo);
                                }
                            }
                        }
                    }
                } else {
                    System.err.println("Invalid configuration file.");
                }
            } else {
                System.err.println("Alert file does not exist. Please generate the file first.");
                generateConfigFile();
                return getAlertsAndEmailsByInstance(instance);
            }
        } catch (IOException e) {
            System.err.println("Failed to retrieve alerts and emails for the instance: " + e.getMessage());
        }

        return alertEmailInfos;
    }

    private List<String> getEmailsByReceiverName(String receiverName, Map<String, Object> config) {
        List<String> emails = new ArrayList<>();

        List<Map<String, Object>> receivers = (List<Map<String, Object>>) config.get("receivers");

        if (receivers != null) {
            for (Map<String, Object> receiver : receivers) {
                String name = (String) receiver.get("name");

                if (receiverName.equals(name)) {
                    List<Map<String, String>> emailConfigs = (List<Map<String, String>>) receiver.get("email_configs");

                    if (emailConfigs != null) {
                        for (Map<String, String> emailConfig : emailConfigs) {
                            String email = emailConfig.get("to");
                            if (email != null) {
                                emails.add(email);
                            }
                        }
                    }

                    break;
                }
            }
        }

        return emails;
    }

}



