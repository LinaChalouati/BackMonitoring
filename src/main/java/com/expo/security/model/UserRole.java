package com.expo.security.model;

import com.expo.project.model.Project;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

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
    @Column(nullable = false)
    private UserRoleType role;

}
