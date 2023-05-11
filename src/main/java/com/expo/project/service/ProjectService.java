package com.expo.project.service;

import com.expo.project.model.Project;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface ProjectService {
    void saveProject(Project project);
    Project getProjectById(Integer id);


    Project getProjectByName(String name);

    List<Project> getAllProjects();
     List<Map<String, String>> getIpAddressesByProjectId(Long projectId);
}
