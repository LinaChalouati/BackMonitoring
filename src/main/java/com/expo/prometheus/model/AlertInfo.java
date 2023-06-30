package com.expo.prometheus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlertInfo {
    private String alertname;
    private String instance;
    private String job;
    private String severity;
    private String state;
    private String activeAt;


}
