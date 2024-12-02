package com.example.int221integratedkk1_backend.Repositories.Taskboard;

import com.example.int221integratedkk1_backend.Entities.Taskboard.InvitationEntity;
import com.example.int221integratedkk1_backend.Entities.Taskboard.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    List<InvitationEntity> findByBoard_IdAndStatus(String boardId, InvitationStatus status);
    boolean existsByBoard_IdAndCollaboratorEmailAndStatus(String boardId, String collaboratorEmail, InvitationStatus status);
}
