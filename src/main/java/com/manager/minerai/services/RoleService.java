package com.manager.minerai.services;

import com.manager.minerai.domain.Permission;
import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.ProjectRole;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.role.AssignPermissionsRequest;
import com.manager.minerai.dto.request.role.CreateRoleRequest;
import com.manager.minerai.dto.response.RoleResponse;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.exception.BadRequestException;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final ProjectRoleRepository projectRoleRepository;
    private final PermissionRepository permissionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public RoleResponse createRole(String projectId, CreateRoleRequest request) {
        User currentUser = getCurrentUser();
        Project project = getProjectAndCheckOwner(projectId, currentUser);

        if (projectRoleRepository.existsByNameAndProjectId(request.getName(), projectId)) {
            throw new BadRequestException("Role with this name already exists in the project");
        }

        ProjectRole role = ProjectRole.builder()
                .name(request.getName())
                .project(project)
                .build();

        projectRoleRepository.save(role);
        return mapToResponse(role);
    }

    public List<RoleResponse> getRoles(String projectId) {
        return projectRoleRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public RoleResponse assignPermissions(String projectId, String roleId,
                                          AssignPermissionsRequest request) {
        User currentUser = getCurrentUser();
        getProjectAndCheckOwner(projectId, currentUser);

        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (!role.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Role does not belong to this project");
        }

        List<Permission> newPermissions = request.getPermissions().stream()
                .map(permissionType -> Permission.builder()
                        .permissionType(permissionType)
                        .projectRole(role)
                        .build())
                .toList();

        role.updatePermissions(newPermissions);
        projectRoleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public void deleteRole(String projectId, String roleId) {
        User currentUser = getCurrentUser();
        getProjectAndCheckOwner(projectId, currentUser);

        ProjectRole role = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (!role.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Role does not belong to this project");
        }

        permissionRepository.deleteByProjectRoleId(roleId);
        projectRoleRepository.delete(role);
    }

    private Project getProjectAndCheckOwner(String projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can manage roles");
        }
        return project;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private RoleResponse mapToResponse(ProjectRole role) {
        List<PermissionType> permissions =
                role.getPermissions() == null ? List.of() :
                        role.getPermissions().stream()
                                .map(Permission::getPermissionType)
                                .toList();

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .projectId(role.getProject().getId())
                .permissions(permissions)
                .build();
    }
}
