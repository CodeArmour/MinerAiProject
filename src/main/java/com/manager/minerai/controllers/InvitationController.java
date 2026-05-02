package com.manager.minerai.controllers;

import com.manager.minerai.dto.request.invitation.InviteMemberRequest;
import com.manager.minerai.dto.response.InvitationResponse;
import com.manager.minerai.services.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * Invite a member to a project by email
     */
    @PostMapping("/projects/{projectId}/invitations")
    public ResponseEntity<InvitationResponse> inviteMember(
            @PathVariable String projectId,
            @Valid @RequestBody InviteMemberRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.inviteMember(projectId, request));
    }

    /**
     * Get all pending invitations for a project
     */
    @GetMapping("/projects/{projectId}/invitations")
    public ResponseEntity<List<InvitationResponse>> getProjectInvitations(
            @PathVariable String projectId
    ) {
        return ResponseEntity.ok(invitationService.getProjectInvitations(projectId));
    }

    /**
     * Get current user's pending invitations
     */
    @GetMapping("/invitations/my-pending")
    public ResponseEntity<List<InvitationResponse>> getMyPendingInvitations() {
        return ResponseEntity.ok(invitationService.getMyPendingInvitations());
    }

    /**
     * Accept an invitation (for both existing and new users after signup)
     */
    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<Void> acceptInvitation(@PathVariable String token) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok().build();
    }

    /**
     * Reject an invitation
     */
    @PostMapping("/invitations/{token}/reject")
    public ResponseEntity<Void> rejectInvitation(@PathVariable String token) {
        invitationService.rejectInvitation(token);
        return ResponseEntity.ok().build();
    }

    /**
     * Cancel an invitation (project owner/admin only)
     */
    @DeleteMapping("/projects/{projectId}/invitations/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable String projectId,
            @PathVariable String invitationId
    ) {
        invitationService.cancelInvitation(projectId, invitationId);
        return ResponseEntity.noContent().build();
    }
}
