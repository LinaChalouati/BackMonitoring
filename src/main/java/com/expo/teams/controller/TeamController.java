package com.expo.teams.controller;

import com.expo.security.model.User;
import com.expo.teams.model.Team;
import com.expo.teams.repo.TeamRepository;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@Controller
@RequestMapping("api/teams")
public class TeamController {
    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GetMapping("get_teams")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return ResponseEntity.ok(teams);
    }
    @PostMapping("add_team")
    public ResponseEntity addTeam(@RequestBody Team team) {

        return ResponseEntity.ok(teamRepository.save(team));
    }


    @DeleteMapping("/delete_team")
    public ResponseEntity<Boolean> deleteTeam(@RequestParam String teamname) {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamname);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            teamRepository.delete(team);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }


   /* @PutMapping("/update_user")
    public ResponseEntity<Boolean> updateUser(@RequestParam String email, @RequestBody User updatedUser) {
        System.out.println("User Firstname: " + updatedUser.getFirstname());
        System.out.println("User Password: " + updatedUser.getPassword());
        System.out.println("User Email: " + updatedUser.getEmail());
        System.out.println("User Lastname: " + updatedUser.getLastname());
        System.out.println("User Role: " + updatedUser.getRole());
        System.out.println("Email: " + email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            userRepository.save(user);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }*/
  /*  @GetMapping("teams_users")
    public List<User> getUsersByTeamId(Long teamId) {
        return teamRepository.findUsersById(teamId);
    }*/

}
