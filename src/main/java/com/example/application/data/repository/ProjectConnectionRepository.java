package com.example.application.data.repository;

import com.example.application.data.entity.ProjectConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectConnectionRepository extends JpaRepository<ProjectConnection, Long> {
    Optional<ProjectConnection> findByName(String name);

}
