        package com.expo.project.model;

        import com.expo.login.model.Team;
        import com.expo.login.model.User;
        import com.fasterxml.jackson.annotation.JsonProperty;
        import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
        import jakarta.persistence.*;
        import lombok.AllArgsConstructor;
        import lombok.Getter;
        import lombok.Setter;
        import org.hibernate.annotations.Type;

        import javax.validation.constraints.NotBlank;
        import java.util.List;
        import java.util.Map;

        import javax.validation.constraints.NotEmpty;
        import javax.validation.constraints.NotNull;

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
