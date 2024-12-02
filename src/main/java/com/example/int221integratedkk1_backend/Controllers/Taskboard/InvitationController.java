package com.example.int221integratedkk1_backend.Controllers.Taskboard;

import com.example.int221integratedkk1_backend.DTOS.InviteCollaboratorRequest;
import com.example.int221integratedkk1_backend.DTOS.InviteCollaboratorResponse;
import com.example.int221integratedkk1_backend.Services.Account.JwtTokenUtil;
import com.example.int221integratedkk1_backend.Services.Taskboard.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23kk1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th:8080", "http://ip23kk1.sit.kmutt.ac.th:8080"})
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/boards/{boardId}/invite")
    public ResponseEntity<?> inviteCollaborator(@PathVariable String boardId, @RequestBody InviteCollaboratorRequest request, @RequestHeader("Authorization") String token) {
        String userId = jwtTokenUtil.getUserIdFromToken(token.substring(7));

        InviteCollaboratorResponse result = invitationService.inviteCollaborator(boardId, request.getCollaboratorEmail(), request.getAccessRight(), userId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long invitationId) {
        InviteCollaboratorResponse result = invitationService.acceptInvitation(invitationId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable Long invitationId) {
        InviteCollaboratorResponse result = invitationService.declineInvitation(invitationId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PutMapping("/invitations/{invitationId}/edit")
    public ResponseEntity<?> editInvitation(@PathVariable Long invitationId, @RequestBody InviteCollaboratorRequest request) {
        InviteCollaboratorResponse result = invitationService.editInvitation(invitationId, request.getStatus(), request.getAccessRight());
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping("/boards/{boardId}/invitations")
    public ResponseEntity<?> getPendingInvitations(@PathVariable String boardId) {
        InviteCollaboratorResponse result = invitationService.getPendingInvitations(boardId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @DeleteMapping("/invitations/{invitationId}/cancel")
    public ResponseEntity<?> cancelInvitation(@PathVariable Long invitationId, @RequestHeader("Authorization") String token) {
        String userId = jwtTokenUtil.getUserIdFromToken(token.substring(7));

        InviteCollaboratorResponse response = invitationService.cancelInvitation(invitationId, userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
