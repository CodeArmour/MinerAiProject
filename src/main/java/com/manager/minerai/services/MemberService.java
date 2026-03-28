package com.manager.minerai.services;

import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.ProjectMember;
import com.manager.minerai.domain.ProjectRole;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.role.AddMemberRequest;
import com.manager.minerai.dto.request.role.UpdateMemberRoleRequest;
import com.manager.minerai.dto.response.MemberResponse;
import com.manager.minerai.exception.BadRequestException;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.ProjectMemberRepository;
import com.manager.minerai.repository.ProjectRepository;
import com.manager.minerai.repository.ProjectRoleRepository;
import com.manager.minerai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final UserRepository userRepository;

    public MemberResponse addMember (String projectId, AddMemberRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project Owner can add members");
        }

        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        if (projectMemberRepository.existsByUserIdAndProjectId(userToAdd.getId(), projectId)) {
            throw new BadRequestException("User already a member of this project");
        }

        ProjectRole role = projectRoleRepository.findById(request.getRoleId())
                .orElseThrow(()->new ResourceNotFoundException("Role not fond"));
        if(!role.getProject().getId().equals(projectId)){
            throw new ForbiddenException("Role does not belong to this project");
        }

        ProjectMember member = ProjectMember.builder()
                .user(userToAdd)
                .project(project)
                .projectRole(role)
                .build();

        projectMemberRepository.save(member);
        return mapToResponse(member);
    }

    public List<MemberResponse> getMembers(String projectId) {
        return projectMemberRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MemberResponse updateMemberRole(String projectId, String memberId,
                                           UpdateMemberRoleRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can update member roles");
        }

        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        ProjectRole newRole = projectRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (!newRole.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Role does not belong to this project");
        }

        member.setProjectRole(newRole);
        projectMemberRepository.save(member);
        return mapToResponse(member);
    }

    public void removeMember(String projectId, String memberId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can remove members");
        }

        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!member.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Member does not belong to this project");
        }

        projectMemberRepository.delete(member);
    }



    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private MemberResponse mapToResponse(ProjectMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userFullName(member.getUser().getFullName())
                .userEmail(member.getUser().getEmail())
                .projectId(member.getProject().getId())
                .roleId(member.getProjectRole().getId())
                .roleName(member.getProjectRole().getName())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
