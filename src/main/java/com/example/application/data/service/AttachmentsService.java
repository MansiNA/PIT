package com.example.application.data.service;

import com.example.application.data.entity.Attachments;
import com.example.application.data.entity.KnowledgeBase;
import com.example.application.data.repository.AttachmentsRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttachmentsService {

    AttachmentsRepository repository;
    public AttachmentsService(AttachmentsRepository repository) {
        this.repository=repository;

    }

    public Optional<Attachments> get(Long id) {
        return repository.findById(id);
    }


    public void delete(Long id) {
        repository.deleteById(id);
    }

    public int count() {
        return (int) repository.count();
    }

    public List<Attachments> findAll() {
        return repository.findAll();
    }

    public Optional<Attachments> findById(long id) {
        return repository.findById(id);
    }

}
