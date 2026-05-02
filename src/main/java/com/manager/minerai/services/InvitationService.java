package com.manager.minerai.services;

import com.manager.minerai.domain.*;
import com.manager.minerai.dto.request.invitation.InviteMemberRequest;
import com.manager.minerai.dto.response.InvitationResponse;
import com.manager.minerai.enums.InvitationStatus;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.exception.BadRequestException;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectRoleRepository roleRepository;
    private final PermissionService permissionService;
    private final EmailService emailService;

    @Transactional
    public InvitationResponse inviteMember(String projectId, InviteMemberRequest request) {
        // Check permission - only project owners or members with ADD_MEMBER permission
        permissionService.checkPermission(projectId, PermissionType.ADD_MEMBER);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User invitedBy = permissionService.getCurrentUser();

        // Check if user is already a member
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            User existingUser = userRepository.findByEmail(request.getEmail()).get();
            if (memberRepository.existsByProjectIdAndUserId(projectId, existingUser.getId())) {
                throw new BadRequestException("User is already a member of this project");
            }
        }

        // Check if there's already a pending invitation
        if (invitationRepository.existsByProjectIdAndEmailAndStatus(
                projectId, request.getEmail(), InvitationStatus.PENDING)) {
            throw new BadRequestException("An invitation has already been sent to this email");
        }

        // Get role if specified
        ProjectRole role = null;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            if (!role.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Role does not belong to this project");
            }
        }

        // Create invitation
        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .email(request.getEmail())
                .token(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .invitedBy(invitedBy)
                .role(role)
                .build();

        invitationRepository.save(invitation);

        // Check if user exists
        boolean userExists = userRepository.findByEmail(request.getEmail()).isPresent();

        // Send invitation email
        emailService.sendInvitationEmail(
                request.getEmail(),
                project.getName(),
                invitedBy.getFullName(),
                invitation.getToken(),
                userExists
        );

        return mapToResponse(invitation);
    }

    public List<InvitationResponse> getProjectInvitations(String projectId) {
        permissionService.checkPermission(projectId, PermissionType.ADD_MEMBER);

        return invitationRepository.findByProjectIdAndStatus(projectId, InvitationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<InvitationResponse> getMyPendingInvitations() {
        User currentUser = permissionService.getCurrentUser();
        return invitationRepository.findByEmailAndStatus(
                        currentUser.getEmail(), InvitationStatus.PENDING)
                .stream()
                .filter(inv -> LocalDateTime.now().isBefore(inv.getExpiresAt()))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void acceptInvitation(String token) {
        ProjectInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation has already been " + invitation.getStatus().name().toLowerCase());
        }

        if (LocalDateTime.now().isAfter(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        User currentUser = permissionService.getCurrentUser();

        if (!currentUser.getEmail().equals(invitation.getEmail())) {
            throw new ForbiddenException("This invitation was sent to a different email address");
        }

        // Check if already a member
        if (memberRepository.existsByProjectIdAndUserId(
                invitation.getProject().getId(), currentUser.getId())) {
            throw new BadRequestException("You are already a member of this project");
        }

        // Add user to project
        ProjectMember member = ProjectMember.builder()
                .project(invitation.getProject())
                .user(currentUser)
                .projectRole(invitation.getRole())
                .build();

        memberRepository.save(member);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Transactional
    public void rejectInvitation(String token) {
        User currentUser = permissionService.getCurrentUser();

        ProjectInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!currentUser.getEmail().equals(invitation.getEmail())) {
            throw new ForbiddenException("This invitation was sent to a different email address");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation has already been " + invitation.getStatus().name().toLowerCase());
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancelInvitation(String projectId, String invitationId) {
        permissionService.checkPermission(projectId, PermissionType.ADD_MEMBER);

        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Invitation does not belong to this project");
        }

        invitationRepository.delete(invitation);
    }

    private InvitationResponse mapToResponse(ProjectInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .token(invitation.getToken())
                .status(invitation.getStatus())
                .projectId(invitation.getProject().getId())
                .projectName(invitation.getProject().getName())
                .invitedByName(invitation.getInvitedBy().getFullName())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .acceptedAt(invitation.getAcceptedAt())
                .build();
    }
}
