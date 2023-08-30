package com.example.application.data.repository;

import com.example.application.data.entity.AgentJobs;
import com.example.application.data.entity.Attachments;
import com.example.application.data.entity.ProductHierarchie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgentJobsRepository extends JpaRepository<AgentJobs, Long> {

    //@Query("select j from AgentJobs j where lower(j.name) = lower(:searchTerm)")
    @Query("select j from AgentJobs j where (j.name) in (:searchTerm)")
    //List<AgentJobs> search(@Param("searchTerm") String searchTerm);
    AgentJobs search(@Param("searchTerm") String searchTerm);


}
