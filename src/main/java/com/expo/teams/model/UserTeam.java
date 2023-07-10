    package com.expo.teams.model;


    import com.expo.security.model.User;
    import jakarta.persistence.*;
    import lombok.*;

    @Entity
    @Table(name = "user_teams")
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    @Builder
    public class UserTeam {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @JoinColumn(name = "team_id")
        private Team team;



    }
