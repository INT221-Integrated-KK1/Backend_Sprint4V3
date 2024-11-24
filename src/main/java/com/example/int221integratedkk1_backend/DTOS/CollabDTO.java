package com.example.int221integratedkk1_backend.DTOS;

import com.example.int221integratedkk1_backend.Entities.Taskboard.AccessRight;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class CollabDTO {
    private String oid;
    private String name;
    private String email;
    private AccessRight accessRight;
    private Timestamp addedOn;

}