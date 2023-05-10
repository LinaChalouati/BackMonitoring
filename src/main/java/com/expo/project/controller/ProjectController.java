package com.expo.project.controller;


import com.expo.project.model.Project;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping("/save-doproject")
    public ResponseEntity<?> saveDoProject(@RequestBody Project project) {
        try {
            System.out.println(project.getProjectName());
            System.out.println(project.getIpAddresses());
            System.out.println(project.getAppType());

            projectService.saveProject(project);
            System.out.println(project);
            return new ResponseEntity<>("Project saved successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving project: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
