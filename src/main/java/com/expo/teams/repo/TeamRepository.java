package com.expo.teams.repo;

import com.expo.security.model.User;
import com.expo.teams.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
   // List<Team> findByTeamNameContainingIgnoreCase(String teamName);
   // Team getAll();
   //
   /*
   @Query("SELECT t FROM Team t INNER JOIN t.projects p WHERE p.projectName = :projectName")
   Team getTeamByProjectName(@Param("projectName") String projectName);*/
 //  List<Team> findByProjects_ProjectName(String projectName);

    //List<User> findUsersById(Long teamId);
   // List<Team> getAll();
    Optional<Team> findByTeamName(String teamName);
 //   Optional<Team> findByTeamNameIgnoreCase(String teamName);




}

