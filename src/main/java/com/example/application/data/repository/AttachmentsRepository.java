package com.example.application.data.repository;

import com.example.application.data.entity.Attachments;
import com.example.application.data.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentsRepository extends JpaRepository<Attachments, Long> {
}
