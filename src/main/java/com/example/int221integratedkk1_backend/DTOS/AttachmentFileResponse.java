package com.example.int221integratedkk1_backend.DTOS;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentFileResponse {
    private  List<AttachmentResponseDTO> data;
    private Integer status;
    private String message;
}
