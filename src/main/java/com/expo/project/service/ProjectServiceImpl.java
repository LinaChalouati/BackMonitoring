package com.expo.project.service;

import com.expo.project.model.Project;
import com.expo.project.repo.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    @Override
    public Project getProjectById(Integer id) {
        Optional<Project> project = projectRepository.findById(Long.valueOf(id));
        return project.orElse(null);
    }
    @Override
    public Project getProjectByName(String projectName) {
        Optional<Project> project = Optional.ofNullable(projectRepository.findByProjectName(projectName));
        return project.orElse(null);
    }


    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    @Override
    public List<Map<String, String>> getIpAddressesByProjectId(Long projectId) {
        List<Map<String, String>> result = new ArrayList<>();
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            String[] ipAddresses = project.getIpAddresses().split(",");
            for (String ipAddress : ipAddresses) {
                String[] parts = ipAddress.split(":");
                if (parts.length == 2) {
                    String address = parts[0].trim();
                    String port = parts[1].trim();
                    Map<String, String> addressAndPort = new HashMap<>();
                    addressAndPort.put("address", address);
                    addressAndPort.put("port", port);
                    result.add(addressAndPort);
                }
            }
        }
        return result;
    }



}
