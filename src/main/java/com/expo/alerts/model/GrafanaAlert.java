package com.expo.alerts.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class GrafanaAlert {
    private Long panelId;
    private String alertName;
    private String alertCondition;
    private List<String> notificationChannels;

    public GrafanaAlert(){}
}
