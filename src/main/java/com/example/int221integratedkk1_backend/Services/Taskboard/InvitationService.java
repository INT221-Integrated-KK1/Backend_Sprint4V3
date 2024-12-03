package com.example.int221integratedkk1_backend.Services.Taskboard;

import com.example.int221integratedkk1_backend.Entities.Account.UsersEntity;
import com.example.int221integratedkk1_backend.Entities.Taskboard.*;
import com.example.int221integratedkk1_backend.Repositories.Account.UserRepository;

import com.example.int221integratedkk1_backend.Repositories.Taskboard.CollabRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.example.int221integratedkk1_backend.DTOS.InviteCollaboratorResponse;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.BoardRepository;
import com.example.int221integratedkk1_backend.Repositories.Taskboard.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InvitationService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollabRepository collabRepository;

    public InviteCollaboratorResponse inviteCollaborator(String boardId, String collaboratorEmail, String accessRight, String userId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();

        // Validate inputs
        if (boardId == null || boardId.trim().isEmpty() || collaboratorEmail == null || collaboratorEmail.trim().isEmpty()) {
            inviteCollaboratorResponse.setMessage("Required Board Id or Collaborator Email");
            inviteCollaboratorResponse.setStatus(400); // Bad Request
            return inviteCollaboratorResponse;
        }

        // Validate mail if exists
        Optional<UsersEntity> userOpt = userRepository.findByEmail(collaboratorEmail);
        if (userOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("User not found.");
            inviteCollaboratorResponse.setStatus(404);
            return inviteCollaboratorResponse;
        }

        UsersEntity user = userOpt.get();
        // Find the board
        Optional<BoardEntity> boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("Board not found");
            inviteCollaboratorResponse.setStatus(404);
            return inviteCollaboratorResponse;
        }

        BoardEntity board = boardOpt.get();

        // Verify if the requesting user is the board owner
        if (!board.getOwnerId().equals(userId)) {
            inviteCollaboratorResponse.setMessage("Only the board owner can add collaborators.");
            inviteCollaboratorResponse.setStatus(403); // Forbidden
            return inviteCollaboratorResponse;
        }

        // Check if there is already a PENDING invitation for this board and email
        boolean isPending = invitationRepository.existsByBoard_IdAndCollaboratorEmailAndStatus(boardId, collaboratorEmail, InvitationStatus.PENDING);
        if (isPending) {
            inviteCollaboratorResponse.setMessage("The user is already a pending collaborator of this board");
            inviteCollaboratorResponse.setStatus(409); // Conflict
            return inviteCollaboratorResponse;
        }

        // Create the invitation
        InvitationEntity invitation = new InvitationEntity();
        invitation.setBoard(board);
        invitation.setCollaboratorEmail(collaboratorEmail);
        invitation.setStatus(InvitationStatus.PENDING);

        // Validate and set access right
        if (StringUtils.endsWithIgnoreCase(accessRight, String.valueOf(AccessRight.READ))) {
            invitation.setAccessRight(AccessRight.READ);
        } else if (StringUtils.endsWithIgnoreCase(accessRight, String.valueOf(AccessRight.WRITE))) {
            invitation.setAccessRight(AccessRight.WRITE);
        } else {
            inviteCollaboratorResponse.setMessage("AccessRight incorrect. Please check format (READ, WRITE) only.");
            inviteCollaboratorResponse.setStatus(400); // Bad Request
            return inviteCollaboratorResponse;
        }


        invitationRepository.save(invitation);
        String inviterName = userRepository.findById(userId)
                .map(UsersEntity::getName)
                .orElse("Unknown User");

        sendInvitationEmail(collaboratorEmail, board, invitation, inviterName);

        inviteCollaboratorResponse.setMessage("Invitation sent to " + collaboratorEmail);
        inviteCollaboratorResponse.setStatus(200); // Success
        return inviteCollaboratorResponse;
    }


//    @Transactional
//    public InviteCollaboratorResponse acceptInvitation(Long invitationId) {
//
//        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();
//        if(Objects.isNull(invitationId)){
//            inviteCollaboratorResponse.setMessage("InvitationId Can't be Null");
//            inviteCollaboratorResponse.setStatus(400);
//            return inviteCollaboratorResponse;
//        }
//        Optional<InvitationEntity> invitationOpt = invitationRepository.findById(invitationId);
//        if (invitationOpt.isEmpty()) {
//            inviteCollaboratorResponse.setMessage("Invitation not found");
//            inviteCollaboratorResponse.setStatus(404);
//            return inviteCollaboratorResponse;
//        }
//
//        InvitationEntity invitation = invitationOpt.get();
//        invitation.setStatus(InvitationStatus.ACCEPTED);
//
//        Collaborator collaborator = new Collaborator();
//        collaborator.setBoard(invitation.getBoard());
//        collaborator.setCollabsEmail(invitation.getCollaboratorEmail());
//        collaborator.setAccessLevel(invitation.getAccessRight());
//        collaborator.setCollabsName(userRepository.findByEmail(invitation.getCollaboratorEmail())
//                .map(user -> user.getName())
//                .orElse("Unknown User"));
//        collaborator.setOwnerId(invitation.getBoard().getOwnerId());
//        collaborator.setCollabsId(userRepository.findByEmail(invitation.getCollaboratorEmail())
//                .map(user -> user.getOid()).orElse(null));
//        collaborator.setAddedOn(new Timestamp(System.currentTimeMillis()));
//
//        collabRepository.save(collaborator);
//
//        inviteCollaboratorResponse.setMessage("Invitation accepted success.");
//        inviteCollaboratorResponse.setStatus(200);
//        return inviteCollaboratorResponse;
//    }

    @Transactional
    public InviteCollaboratorResponse acceptInvitation(Long invitationId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();

        // Validate invitationId
        if (Objects.isNull(invitationId)) {
            inviteCollaboratorResponse.setMessage("InvitationId can't be null.");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }

        // Find the invitation
        Optional<InvitationEntity> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("Invitation not found.");
            inviteCollaboratorResponse.setStatus(404);
            return inviteCollaboratorResponse;
        }

        InvitationEntity invitation = invitationOpt.get();
        log.info("Invitation Details: {}", invitation);

        // Check if the email exists
        Optional<UsersEntity> userOpt = userRepository.findByEmail(invitation.getCollaboratorEmail());
        if (userOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("Collaborator email not found in the system.");
            inviteCollaboratorResponse.setStatus(404);
            return inviteCollaboratorResponse;
        }

        UsersEntity user = userOpt.get();
        log.info("User Details: {}", user);

        // Update invitation status to ACCEPTED
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation); // Save the updated status
        log.info("Invitation status updated to ACCEPTED.");

        // Create a new collaborator
        Collaborator collaborator = new Collaborator();
        collaborator.setBoard(invitation.getBoard());
        collaborator.setCollabsEmail(invitation.getCollaboratorEmail());
        collaborator.setCollabsName(user.getName());
        collaborator.setCollabsId(user.getOid()); // Use Oid from UsersEntity
        collaborator.setAccessLevel(invitation.getAccessRight());
        collaborator.setOwnerId(invitation.getBoard().getOwnerId());
        collaborator.setAddedOn(new Timestamp(System.currentTimeMillis()));

        log.info("Collaborator Details: {}", collaborator);

        // Save the collaborator
        try {
            collabRepository.save(collaborator);
            log.info("Collaborator saved successfully.");
        } catch (Exception e) {
            log.error("Error while saving collaborator: {}", e.getMessage());
            inviteCollaboratorResponse.setMessage("Failed to add collaborator: " + e.getMessage());
            inviteCollaboratorResponse.setStatus(500);
            return inviteCollaboratorResponse;
        }

        inviteCollaboratorResponse.setMessage("Invitation accepted successfully.");
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
        invitationRepository.delete(invitation);

        inviteCollaboratorResponse.setMessage("Invitation declined success.");
        inviteCollaboratorResponse.setStatus(200);
        return inviteCollaboratorResponse;
    }


//    public InviteCollaboratorResponse getPendingInvitations(String boardId) {
//        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();
//        if(Objects.nonNull(boardId)){
//            List<InvitationEntity> invitationEntityList = invitationRepository.findByBoard_IdAndStatus(boardId, InvitationStatus.PENDING);
//            inviteCollaboratorResponse.setMessage("Get pending Invitation Success");
//            //inviteCollaboratorResponse.setData(invitationEntityList);
//            inviteCollaboratorResponse.setStatus(200);
//
//            return inviteCollaboratorResponse;
//        }else{
//            inviteCollaboratorResponse.setMessage("Board Id can't be null");
//            inviteCollaboratorResponse.setStatus(400);
//            return inviteCollaboratorResponse;
//        }
//    }

    public InviteCollaboratorResponse getPendingInvitations(String boardId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();

        if (Objects.isNull(boardId) || boardId.trim().isEmpty()) {

            inviteCollaboratorResponse.setMessage("Board Id can't be null");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }


        List<InvitationEntity> invitationEntityList = invitationRepository.findByBoard_IdAndStatus(boardId, InvitationStatus.PENDING);

        List<Map<String, Object>> pendingInvitations = invitationEntityList.stream().map(invitation -> {
            Map<String, Object> invitationDetails = new HashMap<>();
            invitationDetails.put("Name", getUsernameFromEmail(invitation.getCollaboratorEmail())); // Fetch username from email
            invitationDetails.put("email", invitation.getCollaboratorEmail());
            invitationDetails.put("accessRight", invitation.getAccessRight().name());
            invitationDetails.put("Status", invitation.getStatus());
            return invitationDetails;
        }).collect(Collectors.toList());

        inviteCollaboratorResponse.setMessage("Get pending Invitation Success");
        inviteCollaboratorResponse.setData(pendingInvitations);
        inviteCollaboratorResponse.setStatus(200);

        return inviteCollaboratorResponse;
    }

//    private void sendInvitationEmail(String collaboratorEmail, BoardEntity board  , InvitationEntity invitation) {
//        String invitationLink = generateInvitationLink(invitation.getId(), board.getId());
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(collaboratorEmail);
//        message.setSubject("Invitation to collaborate on board " + board.getBoardName());
//        message.setText("You have been invited to collaborate on the board: " + board.getBoardName()
//                + "\nPlease click the following link to accept or decline the invitation: "
//                + invitationLink); // Replace <invitation link> with the actual URL
//
//        mailSender.send(message);
//    }
private void sendInvitationEmail(String collaboratorEmail, BoardEntity board, InvitationEntity invitation, String inviterName) {

    String invitationLink = generateInvitationLink(invitation.getId(), board.getId());

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(collaboratorEmail);
    message.setSubject("ITBKK-" + board.getBoardName().toUpperCase() + " Invitation to Collaborate");
    message.setText(inviterName + " has invited you to collaborate with "
            + invitation.getAccessRight() + " access right on '"
            + board.getBoardName() + "' board.\n\n"
            + "You can accept or decline this invitation at: " + invitationLink);

    message.setFrom("noreply@intproj23.sit.kmutt.ac.th");
    message.setReplyTo("noreply@intproj23.sit.kmutt.ac.th");

    try {
        mailSender.send(message);
    } catch (Exception e) {

        System.err.println("Error sending email: " + e.getMessage());
        System.out.println("We could not send an email to " + collaboratorEmail
                + ", he/she can accept the invitation at " + invitationLink);
    }
}

    @Transactional
    public InviteCollaboratorResponse cancelInvitation(Long invitationId, String userId) {
        InviteCollaboratorResponse inviteCollaboratorResponse = new InviteCollaboratorResponse();

        // Validate invitationId
        if (Objects.isNull(invitationId)) {
            inviteCollaboratorResponse.setMessage("InvitationId cannot be null");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }

        // Retrieve the invitation
        Optional<InvitationEntity> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            inviteCollaboratorResponse.setMessage("Invitation not found");
            inviteCollaboratorResponse.setStatus(404);
            return inviteCollaboratorResponse;
        }

        InvitationEntity invitation = invitationOpt.get();

        // Check if the user is the board owner
        if (!invitation.getBoard().getOwnerId().equals(userId)) {
            inviteCollaboratorResponse.setMessage("You do not have permission to cancel this invitation.");
            inviteCollaboratorResponse.setStatus(403);
            return inviteCollaboratorResponse;
        }

        // Check if the invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            inviteCollaboratorResponse.setMessage("Only pending invitations can be canceled.");
            inviteCollaboratorResponse.setStatus(400);
            return inviteCollaboratorResponse;
        }

        // Delete the invitation
        invitationRepository.delete(invitation);

        inviteCollaboratorResponse.setMessage("Invitation canceled successfully.");
        inviteCollaboratorResponse.setStatus(200);
        return inviteCollaboratorResponse;
    }



    // FE แก้ ลิงค์เชิญผ่านเมล

    private String generateInvitationLink(Long invitationId, String boardId) {
        String baseUrl = "http://localhost:5173/board/"+ boardId + "/collab/invitations/" + invitationId; // Backend endpoint for accepting invitations
        return baseUrl;
    }

    private String getUsernameFromEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UsersEntity::getName)
                .orElse("Unknown User");
    }
}
