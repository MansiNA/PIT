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
    private String Description;

    @NotEmpty
    private String Filename;

    @Lob
    private byte[] Filecontent;

    public Attachments() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getFilename() {
        return Filename;
    }

    public void setFilename(String filename) {
        Filename = filename;
    }

    public byte[] getFilecontent() {
        return Filecontent;
    }

    public void setFilecontent(byte[] filecontent) {
        Filecontent = filecontent;
    }

    public Attachments(Long id, String description, String filename, byte[] filecontent) {
        this.id = id;
        Description = description;
        Filename = filename;
        Filecontent = filecontent;
    }
}
