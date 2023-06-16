package com.expo.prometheus.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertInfo {
    private String name;
    private String state;
    private String startsAt;
    private String endsAt;

    public AlertInfo(String name, String state, String startsAt, String endsAt) {
    }


    // Getters and setters
}
