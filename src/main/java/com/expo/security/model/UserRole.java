package com.expo.security.model;

import com.expo.project.model.Project;
import com.expo.security.controller.UserController;
import com.expo.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import com.expo.project.model.Project;
import com.expo.security.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "user_roles")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    private UserProjectRole role;


    public UserRole(User userOptional, Project project, UserProjectRole role) {
    }
}
