package com.example.int221integratedkk1_backend.Services.Taskboard;
import org.apache.commons.lang3.StringUtils;

import com.example.int221integratedkk1_backend.DTOS.InviteCollaboratorResponse;
import com.example.int221integratedkk1_backend.Entities.Taskboard.AccessRight;
import com.example.int221integratedkk1_backend.Entities.Taskboard.BoardEntity;
import com.example.int221integratedkk1_backend.Entities.Taskboard.InvitationEntity;
import com.example.int221integratedkk1_backend.Entities.Taskboard.InvitationStatus;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.BoardRepository;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.InvitationRepository;
import org.apache.catalina.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class InvitationService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private JavaMailSender mailSender;

    public InviteCollaboratorResponse inviteCollaborator(String boardId, String collaboratorEmail , String accessRight) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();
        if(boardId != null && !boardId.trim().isEmpty() || collaboratorEmail != null && collaboratorEmail.trim().isEmpty()){
            Optional<BoardEntity> boardOpt = boardRepository.findById(boardId);
            if (boardOpt.isEmpty()) {
                inviteCollaboratorResponse.setMessage("Board not found");
                inviteCollaboratorResponse.setStatus(404);
                return inviteCollaboratorResponse;
            }

            BoardEntity board = boardOpt.get();
            InvitationEntity invitation = new InvitationEntity();
            invitation.setBoard(board);
            invitation.setCollaboratorEmail(collaboratorEmail);
            invitation.setStatus(InvitationStatus.PENDING);
            if(StringUtils.endsWithIgnoreCase(accessRight , String.valueOf(AccessRight.READ))){
                invitation.setAccessRight(AccessRight.READ);
            }else if(StringUtils.endsWithIgnoreCase(accessRight , String.valueOf(AccessRight.WRITE))){
                invitation.setAccessRight(AccessRight.WRITE);
            }else{
                inviteCollaboratorResponse.setMessage("AccessRight incorrect Please Check format (READ , WRITE) only");
                inviteCollaboratorResponse.setStatus(400);
                return inviteCollaboratorResponse;
            }

            invitationRepository.save(invitation);
            sendInvitationEmail(collaboratorEmail,board, invitation);
            inviteCollaboratorResponse.setMessage("Invitation sent to " +  collaboratorEmail);
            inviteCollaboratorResponse.setStatus(200);
            return inviteCollaboratorResponse;
        }else{
            inviteCollaboratorResponse.setMessage("Required Board Id or CollaboratorEmail");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }
    }

    @Transactional
    public InviteCollaboratorResponse acceptInvitation(Long invitationId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();
        if(Objects.isNull(invitationId)){
            inviteCollaboratorResponse.setMessage("InvitationId Can't be Null");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }
        Optional<InvitationEntity> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("Invitation not found");
            inviteCollaboratorResponse.setStatus(404);
            return inviteCollaboratorResponse;
        }

        InvitationEntity invitation = invitationOpt.get();
        invitation.setStatus(InvitationStatus.ACCEPTED);
        // Logic to add collaborator to the board could be here.
        inviteCollaboratorResponse.setMessage("Invitation accepted success.");
        inviteCollaboratorResponse.setStatus(200);
        return inviteCollaboratorResponse;
    }

    public InviteCollaboratorResponse editInvitation(Long invitationId, String status, String accessRight) {
        InviteCollaboratorResponse response = new InviteCollaboratorResponse();

        // Validate inputs
        if (invitationId == null || (StringUtils.isBlank(status) && StringUtils.isBlank(accessRight))) {
            response.setMessage("Required invitationId, status, or accessRight");
            response.setStatus(400);
            return response;
        }

        // Find the invitation
        Optional<InvitationEntity> invitationOptional = invitationRepository.findById(invitationId);
        if (invitationOptional.isEmpty()) {
            response.setMessage("Invitation not found");
            response.setStatus(404);
            return response;
        }

        InvitationEntity invitation = invitationOptional.get();

        // Update status
        if (StringUtils.isNotBlank(status)) {
            try {
                InvitationStatus newStatus = InvitationStatus.valueOf(status.toUpperCase());
                invitation.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                response.setMessage("Invalid status. Allowed values are: PENDING, ACCEPTED, DECLINED , CANCELED");
                response.setStatus(400);
                return response;
            }
        }

        // Update accessRight
        if (StringUtils.isNotBlank(accessRight)) {
            try {
                AccessRight newAccessRight = AccessRight.valueOf(accessRight.toUpperCase());
                invitation.setAccessRight(newAccessRight);
            } catch (IllegalArgumentException e) {
                response.setMessage("Invalid accessRight. Allowed values are: READ, WRITE");
                response.setStatus(400);
                return response;
            }
        }

        // Save updated invitation
        invitationRepository.save(invitation);

        response.setMessage("Invitation updated successfully");
        response.setStatus(200);
        return response;
    }

    @Transactional
    public InviteCollaboratorResponse declineInvitation(Long invitationId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();
        if(Objects.isNull(invitationId)){
            inviteCollaboratorResponse.setMessage("InvitationId Can be Null");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }
        Optional<InvitationEntity> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("Invitation not found");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }

        InvitationEntity invitation = invitationOpt.get();
        invitation.setStatus(InvitationStatus.DECLINED);
        // Logic to add collaborator to the board could be here.
        inviteCollaboratorResponse.setMessage("Invitation declined success.");
        inviteCollaboratorResponse.setStatus(200);
        return inviteCollaboratorResponse;
    }

    public InviteCollaboratorResponse getPendingInvitations(String boardId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();
        if(Objects.nonNull(boardId)){
            List<InvitationEntity> invitationEntityList = invitationRepository.findByBoard_IdAndStatus(boardId, InvitationStatus.PENDING);
            inviteCollaboratorResponse.setMessage("Get pending Invitation Success");
            inviteCollaboratorResponse.setData(invitationEntityList);
            inviteCollaboratorResponse.setStatus(200);

            return inviteCollaboratorResponse;
        }else{
            inviteCollaboratorResponse.setMessage("Board Id can't be null");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }
    }

    private void sendInvitationEmail(String collaboratorEmail, BoardEntity board  , InvitationEntity invitation) {
        String invitationLink = generateInvitationLink(invitation.getId());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(collaboratorEmail);
        message.setSubject("Invitation to collaborate on board " + board.getBoardName());
        message.setText("You have been invited to collaborate on the board: " + board.getBoardName()
                + "\nPlease click the following link to accept or decline the invitation: "
                + invitationLink); // Replace <invitation link> with the actual URL

        mailSender.send(message);
    }

    // FE แก้ ลิงค์เชิญผ่านเมล

    private String generateInvitationLink(Long invitationId) {
        String baseUrl = "http://localhost:5173/board/"+ invitationId + "/collab/invitations"; // Backend endpoint for accepting invitations
        return baseUrl;
    }
}
