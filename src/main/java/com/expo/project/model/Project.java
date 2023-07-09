        package com.expo.project.model;

        import com.expo.security.model.User;
        import com.expo.teams.model.Team;
        import jakarta.persistence.*;
        import lombok.AllArgsConstructor;
        import lombok.Getter;
        import lombok.Setter;

        import javax.validation.constraints.NotBlank;
        import javax.validation.constraints.NotNull;
        import java.util.List;

        @Entity
        @Table(name = "projects")
        @AllArgsConstructor
        @Getter
        @Setter
        public class Project {
                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Long id;

                @NotBlank(message = "Project name cannot be blank")
                @Column(name = "project_name")
                private String projectName;

                @NotNull(message = "Monitoring flag cannot be null")
                @Column(name = "monitoring")
                private boolean monitoring;

                @NotNull(message = "Alerting flag cannot be null")
                @Column(name = "alerting")
                private boolean alerting;

                @NotBlank(message = "App type cannot be blank")
                @Column(name = "app_type")
                private String appType;
                //censée list<String> wala haja haka ama mamchetlich
                @NotBlank(message = "IP address cannot be blank")

                @Column(name = "ip_addresses")
                private String ipAddresses;

                @Column(name = "ms_names")
                private String msnames;
                @Column(name="dashboard_uid")
                private String uid;
                @Column(name="deployment")
                private String deployment;

                @ManyToMany
                @JoinTable(
                        name = "project_team_mapping",
                        joinColumns = @JoinColumn(name = "project_id"),
                        inverseJoinColumns = @JoinColumn(name = "team_id")
                )
                private List<Team> teams;


                @ManyToMany(mappedBy = "projects")
                private List<User> users;

                public Project() {}
        }
