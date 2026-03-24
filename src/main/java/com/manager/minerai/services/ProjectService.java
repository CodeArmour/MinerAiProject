package com.manager.minerai.services;


import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.project.CreateProjectRequest;
import com.manager.minerai.dto.request.project.UpdateProjectRequest;
import com.manager.minerai.dto.response.ProjectResponse;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.ProjectRepository;
import com.manager.minerai.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse create(CreateProjectRequest request) {
        User owner = getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        projectRepository.save(project);
        return mapToResponse(project);
    }

    public List<ProjectResponse> getMyProjects() {
        User user = getCurrentUser();
        return projectRepository.findByOwnerId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProjectResponse getById(String projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        return mapToResponse(project);
    }

    public ProjectResponse update(String projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("Project not found"));
        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())){
            throw new ForbiddenException("Only the project owner can update this project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);
        return mapToResponse(project);
    }

    public void delete(String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("Project not found"));

        User currentUser = getCurrentUser();
        if (!project.getOwner().getId().equals(currentUser.getId())){
            throw new ForbiddenException("Only the project owner can delete this project");
        }

        projectRepository.delete(project);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .ownerName(project.getOwner().getFullName())
                .createdAt(project.getCreatedAt())
                .build();
    }
}
