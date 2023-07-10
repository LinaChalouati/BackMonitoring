package com.expo.security.model;

import com.expo.project.model.Project;
import com.expo.teams.model.Team;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

  @Id
  @GeneratedValue
  private Integer id;
  private String firstname;
  private String lastname;
  private String email;
  @JsonIgnore

  private String password;

  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(mappedBy = "user")
  private List<Token> tokens;

  @ManyToMany
  @JoinTable(
          name = "user_teams",
          joinColumns = @JoinColumn(name = "user_id",referencedColumnName = "id"),
          inverseJoinColumns = @JoinColumn(name = "team_id",referencedColumnName = "id")
  )
  private List<Team> teams;


  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
  private List<UserRole> userRoles;

  @ManyToMany
  @JoinTable(
          name = "user_projects",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "project_id")
  )
  private List<Project> projects;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public void addTeam(Team team) {
    teams.add(team);
    team.getUsers().add(this);
  }

  public void removeTeam(Team team) {
    teams.remove(team);
    team.getUsers().remove(this);
  }
}
