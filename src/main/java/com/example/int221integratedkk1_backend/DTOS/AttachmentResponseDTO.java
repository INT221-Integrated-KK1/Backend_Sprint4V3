package com.example.int221integratedkk1_backend.DTOS;

public class AttachmentResponseDTO {
    private String fileName;
    private String status;

    public AttachmentResponseDTO(String fileName, String status) {
        this.fileName = fileName;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
