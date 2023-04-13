package com.grafana.expo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dashboard")
public class DashboardConfiguration {

    private String title;
    private String[] targets;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTargets(String[] targets) {
        this.targets = targets;
    }
}


