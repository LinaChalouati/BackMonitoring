package com.expo.login.repo;

import com.expo.login.model.Team;
import com.expo.login.model.User;
import com.expo.login.model.UserRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //User findByUsername(String username);

  //  List<User> findByTeam(Team team);
    User save(User user);

    User findUserByUsername(User user);
 //   User findAllByAdmin(User user);
    //List<User> findByTeamAndUserRoleType(Team team, UserRoleType roleType);

  @Query("SELECT u FROM User u INNER JOIN u.userRoles ur INNER JOIN ur.project p WHERE p.projectName = :projectName")
  List<User> getUsersByProjectName(@Param("projectName") String projectName);

  @Query("SELECT u.username FROM User u INNER JOIN u.teams t WHERE t.teamName = :teamName")
  List<String> getUsersNamesByTeamName(@Param("teamName") String teamName);
  List<User> findByTeams_TeamName(String teamName);
  List<User> findByProjects_ProjectName(String projectName);

  User findByUsername(String username);
  List<User> findAll();
  //void deleteUserById(Long id);
  //void deleteById(Long id);


}
