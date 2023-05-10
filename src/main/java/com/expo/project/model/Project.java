package com.expo.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @NotBlank
        private String projectName;

        private boolean monitoring;

        private boolean alerting;

        @NotBlank
        private String appType;

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "ipaddresses", joinColumns = @JoinColumn(name = "project_id"))
        @Column(name = "ipaddress")
        private List<String> ipAddresses;
}
