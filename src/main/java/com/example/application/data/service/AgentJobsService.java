package com.example.application.data.service;

import com.example.application.data.entity.AgentJobs;
import com.example.application.data.repository.AgentJobsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentJobsService {
    private final AgentJobsRepository repository;

    public AgentJobsService(AgentJobsRepository repository) {
        this.repository = repository;
    }

    public List<AgentJobs> findAll() {
        return repository.findAll();
    }

    public List<AgentJobs> findbyJobName(String jobName) {
        return repository.search(jobName);
    }

}
