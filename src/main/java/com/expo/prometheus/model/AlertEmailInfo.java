package com.expo.prometheus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AlertEmailInfo {
    private String alertname;
    private String instance;
    private String receiver;

    private List<String> emails;

}
