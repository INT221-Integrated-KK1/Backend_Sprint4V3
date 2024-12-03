package com.example.int221integratedkk1_backend.Entities.Taskboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "Collaborator", schema = "ITB-KK-V3")
public class Collaborator {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
//    @Column(name = "Id")
//    private int id;

    @Column(name = "collabsId", length = 36, nullable = false)
    private String collabsId;

    @Column(name = "collabsName", length = 100, nullable = false)
    private String collabsName;

    @Column(name = "collabsEmail", length = 100, nullable = false)
    private String collabsEmail;

    // Many Collaborators belong to one Board
    @ManyToOne
    @JoinColumn(name = "boardId", referencedColumnName = "boardId", nullable = false)
    private BoardEntity board;

    @Column(name = "ownerId", length = 36, nullable = false)
    private String ownerId;

    // Use AccessRight enum from the separate file
    @Column(name = "accessLevel", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessRight accessLevel;

    @Column(name = "addedOn", nullable = false)
    private Timestamp addedOn;
}
