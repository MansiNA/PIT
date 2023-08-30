package com.example.application.data.service;

import com.example.application.data.entity.AgentJobs;
import com.example.application.data.entity.Project;
import com.example.application.data.repository.ProjectRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<Project> findAll() {
        return repository.findAll();
    }


    public Project search(String jobName) {
        return repository.search(jobName);
    }


}
