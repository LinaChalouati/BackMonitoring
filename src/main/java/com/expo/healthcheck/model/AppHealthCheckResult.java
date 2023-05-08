package com.expo.healthcheck.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@AllArgsConstructor
public class AppHealthCheckResult {
    private String message;
    private boolean success;

}
