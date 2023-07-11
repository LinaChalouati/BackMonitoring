package com.expo.security.repo;

import com.expo.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  User save(User user);

 // User findUserByUsername(User user);
  //   User findAllByAdmin(User user);
  //List<User> findByTeamAndUserRoleType(Team team, UserRoleType roleType);
/*
  @Query("SELECT u FROM User u INNER JOIN u.userRoles ur INNER JOIN ur.project p WHERE p.projectName = :projectName")
  List<User> getUsersByProjectName(@Param("projectName") String projectName);
*/

  //List<User> findByTeams_TeamName(String teamName);
  //List<User> findByProjects_ProjectName(String projectName);

  //User findByUsername(String username);
  List<User> findAll();
/*  @Query("SELECT u FROM User u WHERE u.email = :username")
  User findUserByUsername(@Param("username") String username);*/

    //List<User> findByTeamsTeamNameIgnoreCase(String teamName);



}
