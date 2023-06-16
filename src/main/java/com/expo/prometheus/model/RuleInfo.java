package com.expo.prometheus.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RuleInfo {
    private String name;
    private String query;
    private String duration;
    private String state;


}
