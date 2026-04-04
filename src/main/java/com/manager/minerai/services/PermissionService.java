package com.manager.minerai.services;

import com.manager.minerai.domain.Permission;
import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.ProjectMember;
import com.manager.minerai.domain.User;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.ProjectMemberRepository;
import com.manager.minerai.repository.ProjectRepository;
import com.manager.minerai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public void checkPermission(String projectId, PermissionType permissionType){
        User currentUser = getCurrentUser();

        boolean isOwner = projectRepository.findById(projectId)
                .map(project -> project.getOwner().getId().equals(currentUser.getId()))
                .orElseThrow(()-> new ResourceNotFoundException("Project not found"));

        if (isOwner) return;

        ProjectMember member = projectMemberRepository
                .findByUserIdAndProjectId(currentUser.getId(), projectId)
                .orElseThrow(()->new ForbiddenException("You are not member of this project"));
        boolean hasPermission = member.getProjectRole().getPermissions()
                .stream()
                .anyMatch(p-> p.getPermissionType().equals(permissionType));

        if (!hasPermission) {
            throw  new ForbiddenException("You don't have permission to perform this action");
        }
    }

    public boolean isOwner(String projectId) {
        User currentUser = getCurrentUser();
        return projectRepository.findById(projectId)
                .map(project -> project.getOwner().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
