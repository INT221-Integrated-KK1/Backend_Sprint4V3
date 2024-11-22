package com.example.int221integratedkk1_backend.DTOS;

import java.time.OffsetDateTime;

public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private OffsetDateTime uploadedOn;

    public AttachmentDTO(Long id, String fileName, String fileType, String fileUrl, OffsetDateTime uploadedOn) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
        this.uploadedOn = uploadedOn;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public OffsetDateTime getUploadedOn() {
        return uploadedOn;
    }

    public void setUploadedOn(OffsetDateTime uploadedOn) {
        this.uploadedOn = uploadedOn;
    }
}

