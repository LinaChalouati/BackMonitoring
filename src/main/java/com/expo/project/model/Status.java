package com.expo.project.model;


import lombok.Getter;

@Getter
public enum Status {
    SERVER_UP("SYSTEM_UP"),
    SERVER_DOWN("SYSTEM_DOWN");
    private final String status;
    Status(String status){
        this.status=status;
    }


}
