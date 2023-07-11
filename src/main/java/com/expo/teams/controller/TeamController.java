package com.expo.teams.controller;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectTeam;
import com.expo.project.repo.ProjectRepository;
import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.repo.UserRepository;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.model.TeamRole;
import com.expo.teams.repo.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
    @RequestMapping("api/teams")
public class TeamController {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    public TeamController(TeamRepository teamRepository, UserRepository userRepository, ProjectRepository projectRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/get_teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        List<TeamDTO> teamDTOs = new ArrayList<>();

        for (Team team : teams) {
            TeamDTO teamDTO = new TeamDTO();
            teamDTO.setId(team.getId());
            teamDTO.setTeamName(team.getTeamName());

            List<User> users = team.getUsers();
            List<UserDTO> userDTOs = new ArrayList<>();

            for (User user : users) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setFirstname(user.getFirstname());
                userDTO.setLastname(user.getLastname());
                userDTO.setEmail(user.getEmail());
                userDTO.setRole(user.getRole());
                userDTOs.add(userDTO);
            }

            teamDTO.setUsers(userDTOs);
            teamDTOs.add(teamDTO);
        }

        return ResponseEntity.ok(teamDTOs);
    }

    @PostMapping("add_team")
    public ResponseEntity addTeam(@RequestBody Team team) {

        return ResponseEntity.ok(teamRepository.save(team));
    }


    @DeleteMapping("/delete_team")
    public ResponseEntity<Boolean> deleteTeam(@RequestParam (value="teamname")String teamname) {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamname);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            teamRepository.delete(team);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }
    @PutMapping("/update_team")
    public ResponseEntity<Boolean> updateTeam(@RequestBody Team newteam, @RequestParam(value = "teamname") String teamname) {
        Optional<Team> oldteamOptional = teamRepository.findByTeamName(teamname);
            Team oldteam = oldteamOptional.get();
            oldteam.setTeamName(newteam.getTeamName());

            teamRepository.save(oldteam);
            return ResponseEntity.ok(true);

    }
    @PostMapping("/{teamId}/users/{userId}")
    public ResponseEntity<Boolean> addUserToTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        Optional<User> userOptional = userRepository.findById(Math.toIntExact(userId));

        if (teamOptional.isPresent() && userOptional.isPresent()) {
            Team team = teamOptional.get();
            User user = userOptional.get();

            team.addUser(user);
            teamRepository.save(team);

            return ResponseEntity.ok(true);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }



    @DeleteMapping("/{teamId}/users/{userId}")
    public ResponseEntity<Boolean> removeUserFromTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        Optional<User> userOptional = userRepository.findById(Math.toIntExact(userId));

        if (teamOptional.isPresent() && userOptional.isPresent()) {
            Team team = teamOptional.get();
            User user = userOptional.get();

            team.removeUser(user);
            teamRepository.save(team);

            return ResponseEntity.ok(true);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
        @GetMapping("/{teamId}/users")
    public ResponseEntity<List<UserDTO>> getTeamUsers(@PathVariable Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);

        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            List<User> users = team.getUsers();

            // Convert User objects to UserDTO objects
            List<UserDTO> userDTOs = users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDTOs);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());

        return userDTO;
    }







}
