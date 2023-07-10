package com.expo.security.controller;

import com.expo.project.model.Project;
import com.expo.project.repo.ProjectRepository;
import com.expo.security.model.*;
import com.expo.security.repo.TokenRepository;
import com.expo.security.repo.UserRepository;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
    }
/*
    @GetMapping("/get_all_users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }*/


    @GetMapping("/users")
    public ResponseEntity<ArrayNode> getAllUsers() {
        ArrayNode userDetailsArray = userService.getAllUsers();
        return ResponseEntity.ok(userDetailsArray);
    }

    @DeleteMapping("/delete_users")
    public ResponseEntity<Boolean> deleteUser(@RequestParam String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isPresent()) {
            this.tokenRepository.deleteById(user.get().getId());

            // Then delete the user
            this.userRepository.delete(user.get());

            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @PutMapping("/update_user")
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
    }


 /*   @GetMapping("/get_teams_users")
    public ResponseEntity<List<User>> getUsersByTeamName(@RequestParam String teamName) {
        List<User> users = userRepository.findByTeamsTeamNameIgnoreCase(teamName);
        return ResponseEntity.ok(users);
    }*/
 @PostMapping("/users/{userId}/projects/{projectId}")
 public ResponseEntity<String> addProjectToUser(@PathVariable Integer userId, @PathVariable Long projectId, UserProjectRole role) {
     Optional<User> userOptional = userRepository.findById(userId);
     Optional<Project> projectOptional = projectRepository.findById(projectId);

     if (userOptional.isPresent() && projectOptional.isPresent()) {
         User user = userOptional.get();
         Project project = projectOptional.get();

         // Check if the user already has the project assigned
         boolean isProjectAssigned = user.getProjects().stream()
                 .anyMatch(existingProject -> existingProject.getId().equals(projectId));

         if (isProjectAssigned) {
             return ResponseEntity.badRequest().body("Project is already assigned to the user.");
         }

         // Add the project to the user's list of projects
         user.getProjects().add(project);
         // Add the user to the project's list of users
         project.getUsers().add(user);

         // Assign a role to the user in the project (assuming 'role' is the desired role)
         this.assignRoleToProject(project, role, user);

         userRepository.save(user);
         projectRepository.save(project);

         return ResponseEntity.ok("Project added to user successfully.");
     }

     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or project not found.");
 }


    public void assignRoleToProject(Project project, UserProjectRole role, User user) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setProject(project);
        userRole.setRole(role);

        user.getUserRoles().add(userRole);
        project.getUserRoles().add(userRole);
    }

    @PutMapping("/users/{userId}/projects/{projectId}/{role}")
    public ResponseEntity<String> updateUserRoleInProject(@PathVariable Integer userId, @PathVariable Long projectId, @PathVariable UserProjectRole role) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (userOptional.isPresent() && projectOptional.isPresent()) {
            User user = userOptional.get();
            Project project = projectOptional.get();

            // Find the existing user role in the project
            Optional<UserRole> userRoleOptional = user.getUserRoles().stream()
                    .filter(userRole -> userRole.getProject().equals(project))
                    .findFirst();

            if (userRoleOptional.isPresent()) {
                UserRole userRole = userRoleOptional.get();
                userRole.setRole(role);
                userRepository.save(user);

                return ResponseEntity.ok("User role in project updated successfully.");
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User role in project not found.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or project not found.");
    }

    @DeleteMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<String> removeUserFromProject(@PathVariable Integer userId, @PathVariable Long projectId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (userOptional.isPresent() && projectOptional.isPresent()) {
            User user = userOptional.get();
            Project project = projectOptional.get();

            // Check if the user is assigned to the project
            boolean isUserAssigned = user.getProjects().stream()
                    .anyMatch(existingProject -> existingProject.getId().equals(projectId));

            if (!isUserAssigned) {
                return ResponseEntity.badRequest().body("User is not assigned to the project.");
            }

            // Remove the project from the user's list of projects
            user.getProjects().remove(project);
            // Remove the user from the project's list of users
            project.getUsers().remove(user);

            // Remove the user role in the project
            removeUserRole(user, project);

            userRepository.save(user);
            projectRepository.save(project);

            return ResponseEntity.ok("User removed from the project successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or project not found.");
    }

    private void removeUserRole(User user, Project project) {
        user.getUserRoles().removeIf(userRole -> userRole.getProject().equals(project));
    }

}
