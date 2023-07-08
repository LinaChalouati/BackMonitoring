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
public class MailNotifInfo {
    private String alertname;
    private String instance;
    private List<String> emails;
    private boolean state;
    private String receiver;

}
