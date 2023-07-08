package com.expo.login.repo;

import com.expo.login.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
   // List<Team> findByTeamNameContainingIgnoreCase(String teamName);
   // Team getAll();
    @Query("SELECT t FROM Team t INNER JOIN t.projects p WHERE p.projectName = :projectName")
    Team getTeamByProjectName(@Param("projectName") String projectName);
    List<Team> findByProjects_ProjectName(String projectName);

}

