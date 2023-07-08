package com.expo.login.service;

import com.expo.login.model.Team;
import com.expo.login.model.User;
import com.expo.login.repo.TeamRepository;
import com.github.dockerjava.api.exception.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + teamId));
    }

    public Team saveTeam(Team team) {
        // Additional business logic if needed
        return teamRepository.save(team);
    }
    public void addUserToTeam(User user, Team team) {
        team.getUsers().add(user);
        teamRepository.save(team);
    }

}
