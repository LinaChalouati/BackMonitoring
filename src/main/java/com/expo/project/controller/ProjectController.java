package com.expo.project.controller;


import com.expo.project.model.Project;
import com.expo.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/project")
@CrossOrigin("*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

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
    public ResponseEntity<Project> getProjectById(@RequestParam (value = "id") String id) {
        Project project = projectService.getProjectById(Integer.parseInt(id));
        System.out.println(project);
        if (project != null) {
            return ResponseEntity.ok().body(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
        @GetMapping("/getprojectbyname")
    public ResponseEntity<Project> getProjectByName(@RequestParam (value = "projectname") String project_name) {
        Project project = projectService.getProjectByName(project_name);
        System.out.println(project);
        if (project != null) {
            return ResponseEntity.ok().body(project);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/get-all-projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        if (projects != null) {
            return ResponseEntity.ok().body(projects);
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

}
