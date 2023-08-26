package com.example.application.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(schema = "dbo", name = "attachments")
public class Attachments {
    @Id
    private Long id;

    @NotEmpty
    private String description;

    @NotEmpty
    private String filename;

    @Lob
    private byte[] filecontent;

    public Attachments() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        filename = filename;
    }

    public byte[] getFilecontent() {
        return filecontent;
    }

    public void setFilecontent(byte[] filecontent) {
        filecontent = filecontent;
    }

    public Attachments(Long id, String description, String filename, byte[] filecontent) {
        this.id = id;
        description = description;
        filename = filename;
        filecontent = filecontent;
    }
}
