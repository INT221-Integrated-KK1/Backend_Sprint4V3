package com.example.int221integratedkk1_backend.DTOS;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentResponse {
    private List<AttachmentDTO> data;
    private int status;
    private String message;

    // Constructor
    public AttachmentResponse(List<AttachmentDTO> data, int status, String message) {
        this.data = data;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public List<AttachmentDTO> getData() {
        return data;
    }

    public void setData(List<AttachmentDTO> data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
