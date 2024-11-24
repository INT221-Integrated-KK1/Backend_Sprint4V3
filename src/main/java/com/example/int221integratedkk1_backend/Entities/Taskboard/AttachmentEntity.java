package com.example.int221integratedkk1_backend.Entities.Taskboard;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "Attachment", schema = "ITB-KK-V3")
public class AttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fileName", nullable = false, length = 255)
    private String fileName;

    @Column(name = "fileType", nullable = false, length = 50)
    private String fileType;

    @Column(name = "filePath", nullable = false, length = 255)
    private String filePath;

    @Column(name = "uploadedOn", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime uploadedOn;

    @ManyToOne
    @JoinColumn(name = "taskId", nullable = false)
    private TaskEntity task;
}
