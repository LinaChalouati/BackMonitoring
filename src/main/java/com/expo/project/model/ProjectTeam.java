package com.expo.project.model;


import com.expo.teams.model.Team;
import com.expo.teams.model.TeamRole;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "project_teams")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProjectTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private TeamRole role;
}
