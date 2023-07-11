package com.expo.project.controller;


import com.expo.project.model.Project;
import com.expo.project.model.ProjectDTO;
import com.expo.project.model.ProjectTeam;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.service.ProjectService;
import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.model.UserRole;
import com.expo.security.repo.UserRoleRepository;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.model.TeamRole;
import com.expo.teams.repo.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project")
@CrossOrigin("*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRoleRepository userRoleRepository;
    private final TeamRepository teamRepository;

    public ProjectController(ProjectRepository projectRepository, UserRoleRepository userRoleRepository, TeamRepository teamRepository) {
        this.projectRepository = projectRepository;
        this.userRoleRepository = userRoleRepository;
        this.teamRepository = teamRepository;
    }

    @PostMapping(value = "/save-doproject")
        public ResponseEntity<?> saveDoProject(@RequestBody Project project) {
            try {
                System.out.println(project.getProjectName());
                System.out.println(project.getIpAddresses());

                System.out.println(project.getAppType());
                System.out.println(project.getClass());
                projectService.saveProject(project);
                System.out.println(project);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("message", "Project saved successfully"));

            }catch (Exception e) {
                String errorMsg = "Error saving project: " + e.getMessage() + "\n" + e.toString();
                return new ResponseEntity<>(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
            }

    }
    @GetMapping("/getproject")
    public ResponseEntity<ProjectDTO> getProjectById(@RequestParam(value = "id") String id) {
        Project project = projectService.getProjectById(Integer.parseInt(id));
        if (project != null) {
            ProjectDTO projectDTO = convertToDTO(project); // Convert Project to ProjectDTO
            return ResponseEntity.ok(projectDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getprojectbyname")
    public ResponseEntity<ProjectDTO> getProjectByName(@RequestParam(value = "projectname") String projectName) {
        Project project = projectService.getProjectByName(projectName);

        if (project != null) {
            ProjectDTO projectDTO = convertToDTO(project);
            return ResponseEntity.ok(projectDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all-projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        List<ProjectDTO> projectDTOs = new ArrayList<>();

        for (Project project : projects) {
            ProjectDTO projectDTO = convertToDTO(project);
            projectDTOs.add(projectDTO);
        }

        if (!projectDTOs.isEmpty()) {
            return ResponseEntity.ok(projectDTOs);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping("/projects/{id}/ip-addresses")
    public ResponseEntity<List<Map<String, String>>> getIpAddressesByProjectId(@PathVariable Long id) {
        List<Map<String, String>> ipAddresses = projectService.getIpAddressesByProjectId(Long.valueOf(id));
        if (ipAddresses != null && !ipAddresses.isEmpty()) {
            return ResponseEntity.ok(ipAddresses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setProjectName(project.getProjectName());
        projectDTO.setMonitoring(project.isMonitoring());
        projectDTO.setAlerting(project.isAlerting());
        projectDTO.setAppType(project.getAppType());
        projectDTO.setIpAddresses(project.getIpAddresses());
        projectDTO.setMsnames(project.getMsnames());
        projectDTO.setUid(project.getUid());
        projectDTO.setDeployment(project.getDeployment());
        projectDTO.setIsPrivate(project.getIsPrivate());

        List<UserDTO> userDTOs = project.getUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
        projectDTO.setUsers(userDTOs);
        List<TeamDTO> teamDTOs = project.getTeams().stream()
                .map(this::convertTeamToDTO)
                .collect(Collectors.toList());
        projectDTO.setTeams(teamDTOs);

        // Set other properties as needed


        // Set other properties as needed

        return projectDTO;
    }
    private UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());

        return userDTO;
    }
    private TeamDTO convertTeamToDTO(Team team) {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setId(team.getId());
        teamDTO.setTeamName(team.getTeamName());
        return teamDTO;
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId, @RequestBody Project updatedProject) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            Project existingProject = projectOptional.get();

            // Update the project fields with non-null values from the updatedProject
            if (Objects.nonNull(updatedProject.getProjectName())) {
                existingProject.setProjectName(updatedProject.getProjectName());
            }
            if (Objects.nonNull(updatedProject.getAppType())) {
                existingProject.setAppType(updatedProject.getAppType());
            }
            if (Objects.nonNull(updatedProject.getIpAddresses())) {
                existingProject.setIpAddresses(updatedProject.getIpAddresses());
            }
            if (Objects.nonNull(updatedProject.getMsnames())) {
                existingProject.setMsnames(updatedProject.getMsnames());
            }
            if (Objects.nonNull(updatedProject.isMonitoring())) {
                existingProject.setMonitoring(updatedProject.isMonitoring());
            }
            if (Objects.nonNull(updatedProject.isAlerting())) {
                existingProject.setAlerting(updatedProject.isAlerting());
            }
            if (Objects.nonNull(updatedProject.getIsPrivate())) {
                existingProject.setIsPrivate(updatedProject.getIsPrivate());
            }



            projectRepository.save(existingProject);
            return ResponseEntity.ok("Project updated successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found.");
    }
    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            // Remove user roles associated with the project
            List<UserRole> userRoles = project.getUserRoles();
            for( UserRole role : userRoles){
                userRoleRepository.delete(role);

            }


            // Remove the project from users' list of projects
            List<User> users = project.getUsers();
            for (User user : users) {
                user.getProjects().remove(project);
            }

            // Delete the project
            projectRepository.delete(project);

            return ResponseEntity.ok("Project deleted successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found.");
    }
    // fiha mochkla , moch ifassakh m table projectTeam
    @DeleteMapping("/projects/{projectId}/teams/{teamId}")
    public ResponseEntity<String> removeTeamFromProject(@PathVariable Long projectId, @PathVariable Long teamId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<Team> teamOptional = teamRepository.findById(teamId);

        if (projectOptional.isPresent() && teamOptional.isPresent()) {
            Project project = projectOptional.get();
            Team team = teamOptional.get();

            // Check if the team is assigned to the project
            boolean isTeamAssigned = project.getTeams().stream()
                    .anyMatch(existingTeam -> existingTeam.getId().equals(teamId));

            if (!isTeamAssigned) {
                return ResponseEntity.badRequest().body("Team is not assigned to the project.");
            }

            // Remove the team from the project's list of teams
            project.getTeams().removeIf(existingTeam -> existingTeam.getId().equals(teamId));

            // Remove the project from the team's list of projects
            team.getProjects().remove(project);

            projectRepository.save(project);
            teamRepository.save(team);

            return ResponseEntity.ok("Team removed from the project successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project or team not found.");
    }

    @PostMapping("/projects/{projectId}/teams/{teamId}")
    public ResponseEntity<String> addTeamToProject(@PathVariable Long projectId, @PathVariable Long teamId, @RequestParam TeamRole role) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<Team> teamOptional = teamRepository.findById(teamId);

        if (projectOptional.isPresent() && teamOptional.isPresent()) {
            Project project = projectOptional.get();
            Team team = teamOptional.get();

            // Check if the team is already added to the project
            boolean isTeamAdded = project.getTeams().stream()
                    .anyMatch(existingTeam -> existingTeam.getId().equals(teamId));

            if (isTeamAdded) {
                return ResponseEntity.badRequest().body("Team is already added to the project.");
            }

            // Add the team to the project
            project.getTeams().add(team);
            // Set the role for the team in the project
            this.setTeamRoleInProject(project, team, role);

            projectRepository.save(project);

            return ResponseEntity.ok("Team added to the project successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project or team not found.");
    }

    private void setTeamRoleInProject(Project project, Team team, TeamRole role) {
        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.setProject(project);
        projectTeam.setTeam(team);
        projectTeam.setRole(role);

        project.getProjectTeams().add(projectTeam);
    }




}



