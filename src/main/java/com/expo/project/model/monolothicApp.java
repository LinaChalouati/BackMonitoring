package com.expo.project.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class monolothicApp {
    @Id
    private Long id;

    private String ipaddr;
    private String port;

}
