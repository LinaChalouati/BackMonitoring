package com.expo.login.controller;

import com.expo.login.model.Team;
import com.expo.login.model.User;
import com.expo.login.model.UserRole;
import com.expo.login.model.UserRoleType;
import com.expo.login.repo.TeamRepository;
import com.expo.login.repo.UserRepository;
import com.expo.login.service.TeamService;
import com.expo.login.service.UserRoleService;
import com.expo.login.service.UserService;
import com.expo.project.model.Project;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final TeamService teamService;
    private final ProjectService projectService;
    private final UserRoleService userRoleService;

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;

    public UserController(UserService userService, TeamService teamService, ProjectService projectService, UserRoleService userRoleService, UserRepository userRepository, TeamRepository teamRepository, ProjectRepository projectRepository) {
        this.userService = userService;
        this.teamService = teamService;
        this.projectService = projectService;
        this.userRoleService = userRoleService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
    }

    @PostMapping("/create_user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/{userId}/team/{teamId}")
    public ResponseEntity<User> addUserToTeam(@PathVariable("userId") Long userId, @PathVariable("teamId") Long teamId) {
        User user = userService.getUserById(userId);
        Team team = teamService.getTeamById(teamId);

        // Add the user to the team
        team.getUsers().add(user);

        // Save the updated team
        teamService.saveTeam(team);

        return ResponseEntity.ok(user);
    }


    @PostMapping("/{userId}/project/{projectId}/role/{role}")
    public ResponseEntity<UserRole> assignUserRole(@PathVariable("userId") Long userId, @PathVariable("projectId") Long projectId, @PathVariable("role") String role) {
        User user = userService.getUserById(userId);
        Project project = projectService.getProjectById(Math.toIntExact(projectId));

        UserRoleType userRoleType;
        if (role.equalsIgnoreCase("EDITOR")) {
            userRoleType = UserRoleType.EDITOR;
        } else if (role.equalsIgnoreCase("VIEWER")) {
            userRoleType = UserRoleType.VIEWER;
        } else {
            return ResponseEntity.badRequest().build();
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setProject(project);
        userRole.setRole(userRoleType);

        UserRole createdUserRole = userRoleService.createUserRole(userRole);
        return ResponseEntity.ok(createdUserRole);
    }

    @GetMapping("/users/{teamName}")
    public ResponseEntity<List<String>> getUsersNamesByTeamName(@PathVariable("teamName") String teamName) {
        List<String> userNames = userRepository.getUsersNamesByTeamName(teamName);
        return ResponseEntity.ok(userNames);
    }

 /*   @GetMapping("/users/project/{projectName}")
    public ResponseEntity<List<User>> getUsersByProjectName(@PathVariable("projectName") String projectName) {
        List<User> users = userRepository.getUsersByProjectName(projectName);
        return ResponseEntity.ok(users);
    }
*/
 /*   @GetMapping("/team/project/{projectName}")
    public ResponseEntity<Team> getTeamByProjectName(@PathVariable("projectName") String projectName) {
        Team team = teamRepository.getTeamByProjectName(projectName);
        return ResponseEntity.ok(team);
    }
*/
   @GetMapping("/users/team/{teamName}")
    public ResponseEntity<List<User>> getUsersByTeamName(@PathVariable("teamName") String teamName) {
        List<User> users = userRepository.findByTeams_TeamName(teamName);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/users/project/{projectName}")
    public ResponseEntity<List<User>> getUsersByProjectName(@PathVariable("projectName") String projectName) {
        List<User> users = userRepository.findByProjects_ProjectName(projectName);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/teams/project/{projectName}")
    public ResponseEntity<List<Team>> getTeamByProjectName(@PathVariable("projectName") String projectName) {
        List<Team> teams = teamRepository.findByProjects_ProjectName(projectName);
        return ResponseEntity.ok(teams);
    }



}

