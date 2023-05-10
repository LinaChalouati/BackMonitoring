package com.expo.project.service;

import com.expo.project.model.Project;
import org.springframework.stereotype.Service;

@Service
public interface ProjectService {
    void saveProject(Project project);

}
