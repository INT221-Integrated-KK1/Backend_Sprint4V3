package com.example.int221integratedkk1_backend.Controllers.Taskboard;

import com.example.int221integratedkk1_backend.DTOS.InviteCollaboratorRequest;
import com.example.int221integratedkk1_backend.DTOS.InviteCollaboratorResponse;
import com.example.int221integratedkk1_backend.Entities.Taskboard.InvitationEntity;
import com.example.int221integratedkk1_backend.Services.Account.JwtTokenUtil;
import com.example.int221integratedkk1_backend.Services.Taskboard.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/boards/{boardId}/invite")
    public ResponseEntity<InviteCollaboratorResponse> inviteCollaborator(@PathVariable String boardId, @RequestBody InviteCollaboratorRequest collaboratorEmail) {
        InviteCollaboratorResponse result = invitationService.inviteCollaborator(boardId, collaboratorEmail.getCollaboratorEmail() , collaboratorEmail.getAccessRight());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<InviteCollaboratorResponse> acceptInvitation(@PathVariable Long invitationId) {
        InviteCollaboratorResponse result = invitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<InviteCollaboratorResponse> declineInvitation(@PathVariable Long invitationId) {
        InviteCollaboratorResponse result = invitationService.declineInvitation(invitationId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/invitations/{invitationId}/edit")
    public ResponseEntity<InviteCollaboratorResponse> editInvitation(@PathVariable Long invitationId , @RequestBody InviteCollaboratorRequest inviteCollaboratorRequest) {
        InviteCollaboratorResponse result = invitationService.editInvitation(invitationId , inviteCollaboratorRequest.getStatus() , inviteCollaboratorRequest.getAccessRight());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/boards/{boardId}/invitations")
    public ResponseEntity<InviteCollaboratorResponse> getPendingInvitations(@PathVariable String boardId) {
        // Assuming userId is mapped to collaboratorEmail for simplicity
        InviteCollaboratorResponse pendingInvitations = invitationService.getPendingInvitations(boardId);
        return ResponseEntity.ok(pendingInvitations);
    }


    @DeleteMapping ("/invitations/{invitationId}/cancel")
    public ResponseEntity<?> cancelInvitation(@PathVariable Long invitationId,
                                              @RequestHeader("Authorization") String token) {
        String userId = jwtTokenUtil.getUserIdFromToken(token.substring(7));

        InviteCollaboratorResponse response = invitationService.cancelInvitation(invitationId, userId);

        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
