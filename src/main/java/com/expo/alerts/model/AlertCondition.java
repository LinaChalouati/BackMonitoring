package com.expo.alerts.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


//l partie hedhi bch tetbadel , à savoir , instead i'll be using prometheus for the alertes

@AllArgsConstructor
@Getter
@Setter
@Component
public class AlertCondition {

    private String name;
    private enum ConditionType {
        GREATER_THAN,
        LESS_THAN,
        EQUAL_TO,
        NOT_EQUAL_TO,
        CONTAINS,
        NOT_CONTAINS
    }
    private String message;
    private int frequency;
    private int for_;
    private String DashboardTitle;
    private String PanelTitle;



    public AlertCondition(){}

}
