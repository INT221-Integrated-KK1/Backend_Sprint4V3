package com.example.int221integratedkk1_backend.Entities.Taskboard;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "Invitation", schema = "ITB-KK-V3")
public class InvitationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "boardId", nullable = false)
    private BoardEntity board;

    @Column(name = "collaboratorEmail", nullable = false)
    private String collaboratorEmail;

    @Column(name = "accessRight", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessRight accessRight = AccessRight.READ;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;
}

