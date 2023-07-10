package com.expo.teams.model;

import com.expo.project.model.Project;
import com.expo.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "teams")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamName;

    @ManyToMany(mappedBy = "teams")
    private List<Project> projects;
    @ManyToMany(mappedBy = "teams")
    private List<User> users;



    public void addUser(User user) {
        users.add(user);
        user.getTeams().add(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.getTeams().remove(this);
    }

}

