package com.manager.minerai.services;

import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.ProjectMember;
import com.manager.minerai.domain.Task;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.response.*;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public UserResponse getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .createdAt(user.getCreatedAt())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public OwnerSummaryResponse getOwnerSummary() {
        User user = getCurrentUser();

        List<ProjectSummaryResponse> projects = projectRepository.findByOwnerId(user.getId())
                .stream()
                .map(project -> ProjectSummaryResponse.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .description(project.getDescription())
                        .membersCount(projectMemberRepository.countByProjectId(project.getId()))
                        .tasksCount(taskRepository.countByProjectId(project.getId()))
                        .build())
                .toList();

        return OwnerSummaryResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .totalProjectsOwned(projects.size())
                .projects(projects)
                .build();
    }

    public MemberSummaryResponse getMemberSummary() {
        User user = getCurrentUser();

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());

        List<MemberProjectResponse> projects = memberships.stream()
                .map(member -> MemberProjectResponse.builder()
                        .id(member.getProject().getId())
                        .name(member.getProject().getName())
                        .description(member.getProject().getDescription())
                        .myRole(member.getProjectRole().getName())
                        .build())
                .toList();

        List<AssignedTaskResponse> assignedTasks = taskRepository.findByAssigneeId(user.getId())
                .stream()
                .map(task -> AssignedTaskResponse.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .status(task.getStatus())
                        .projectId(task.getProject().getId())
                        .projectName(task.getProject().getName())
                        .build())
                .toList();

        return MemberSummaryResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .totalProjectsAsMember(projects.size())
                .totalAssignedTasks(assignedTasks.size())
                .projects(projects)
                .assignedTasks(assignedTasks)
                .build();
    }

    public PageResponse<ProjectResponse> getOwnedProjects(int page, int size) {
        User user = getCurrentUser();
        List<ProjectResponse> allProjects = projectRepository.findByOwnerId(user.getId())
                .stream()
                .map(this::mapProjectToResponse)
                .toList();

        int start = page * size;
        int end = Math.min(start + size, allProjects.size());
        List<ProjectResponse> content = (start < allProjects.size()) 
                ? allProjects.subList(start, end) 
                : List.of();

        return PageResponse.<ProjectResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(allProjects.size())
                .totalPages((int) Math.ceil((double) allProjects.size() / size))
                .last(end >= allProjects.size())
                .build();
    }

    public PageResponse<MemberProjectResponse> getMemberProjects(int page, int size) {
        User user = getCurrentUser();
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user.getId());

        List<MemberProjectResponse> allProjects = memberships.stream()
                .map(member -> MemberProjectResponse.builder()
                        .id(member.getProject().getId())
                        .name(member.getProject().getName())
                        .description(member.getProject().getDescription())
                        .myRole(member.getProjectRole().getName())
                        .build())
                .toList();

        int start = page * size;
        int end = Math.min(start + size, allProjects.size());
        List<MemberProjectResponse> content = (start < allProjects.size()) 
                ? allProjects.subList(start, end) 
                : List.of();

        return PageResponse.<MemberProjectResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(allProjects.size())
                .totalPages((int) Math.ceil((double) allProjects.size() / size))
                .last(end >= allProjects.size())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProjectResponse mapProjectToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .owner(UserResponse.builder()
                        .id(project.getOwner().getId())
                        .fullName(project.getOwner().getFullName())
                        .email(project.getOwner().getEmail())
                        .createdAt(project.getOwner().getCreatedAt())
                        .build())
                .labels(project.getLabels() != null ? project.getLabels().stream()
                        .map(label -> LabelResponse.builder()
                                .id(label.getId())
                                .name(label.getName())
                                .color(label.getColor())
                                .type(label.getType())
                                .projectId(project.getId())
                                .createdAt(label.getCreatedAt())
                                .updatedAt(label.getUpdatedAt())
                                .build())
                        .toList() : List.of())
                .createdAt(project.getCreatedAt())
                .updatedAt(null)
                .build();
    }
}