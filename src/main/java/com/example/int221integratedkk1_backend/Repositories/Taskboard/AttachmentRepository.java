package com.example.int221integratedkk1_backend.Repositories.Taskboard;

import com.example.int221integratedkk1_backend.Entities.Taskboard.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
    List<AttachmentEntity> findByTaskId(int taskId);
}
