package com.example.application.data.service;

import com.example.application.data.entity.AgentJobs;
import com.example.application.data.repository.AgentJobsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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

        //Liste aller in Projekt definierter Agent-Jobs

        String[] teile = jobName.split(";");

        List<AgentJobs> angentJobs = new ArrayList<>();
        // Iteriere durch die aufgeteilten Teile und gib sie aus
        for (String teil : teile) {
            System.out.println(teil);

            angentJobs.add(repository.search(teil));

        }


        //return repository.search(jobName);
        return angentJobs;
    }

}
