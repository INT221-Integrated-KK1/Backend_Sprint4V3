package com.example.int221integratedkk1_backend.DTOS;

import lombok.Data;

@Data
public class InviteCollaboratorRequest {
    private String collaboratorEmail;
    private String accessRight;
    private String status;
}
