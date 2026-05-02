package com.manager.minerai.services;

import com.manager.minerai.domain.Label;
import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.Task;
import com.manager.minerai.dto.request.label.CreateLabelRequest;
import com.manager.minerai.dto.request.label.UpdateLabelRequest;
import com.manager.minerai.dto.response.LabelResponse;
import com.manager.minerai.dto.response.ProjectResponse;
import com.manager.minerai.dto.response.TaskResponse;
import com.manager.minerai.dto.response.UserResponse;
import com.manager.minerai.enums.LabelType;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.exception.BadRequestException;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.LabelRepository;
import com.manager.minerai.repository.ProjectRepository;
import com.manager.minerai.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PermissionService permissionService;

    public LabelResponse createLabel(String projectId, CreateLabelRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!permissionService.isOwner(projectId)) {
            throw new ForbiddenException("Only the project owner can create labels");
        }

        if (labelRepository.existsByNameAndProjectId(request.getName(), projectId)) {
            throw new BadRequestException("Label with this name already exists in the project");
        }

        Label label = Label.builder()
                .name(request.getName())
                .color(request.getColor())
                .type(request.getType())
                .project(project)
                .build();

        labelRepository.save(label);
        return mapToLabelResponse(label);
    }

    public List<LabelResponse> getProjectLabels(String projectId) {
        return labelRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToLabelResponse)
                .toList();
    }

    public List<LabelResponse> getProjectLabelsByType(String projectId, LabelType type) {
        return labelRepository.findByProjectIdAndType(projectId, type)
                .stream()
                .map(this::mapToLabelResponse)
                .toList();
    }

    public LabelResponse updateLabel(String projectId, String labelId, UpdateLabelRequest request) {
        if (!permissionService.isOwner(projectId)) {
            throw new ForbiddenException("Only the project owner can update labels");
        }

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (!label.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Label does not belong to this project");
        }

        label.setName(request.getName());
        label.setColor(request.getColor());
        labelRepository.save(label);
        return mapToLabelResponse(label);
    }

    public void deleteLabel(String projectId, String labelId) {
        if (!permissionService.isOwner(projectId)) {
            throw new ForbiddenException("Only the project owner can delete labels");
        }

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (!label.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Label does not belong to this project");
        }

        labelRepository.delete(label);
    }

    @Transactional
    public TaskResponse assignLabelToTask(String projectId, String taskId, String labelId) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_TASK);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (!label.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Label does not belong to this project");
        }

        if (label.getType() != LabelType.TASK) {
            throw new BadRequestException("This label is not a task label");
        }

        if (!task.getLabels().contains(label)) {
            task.getLabels().add(label);
            taskRepository.save(task);
        }

        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse removeLabelFromTask(String projectId, String taskId, String labelId) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_TASK);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        task.getLabels().remove(label);
        taskRepository.save(task);
        return mapToTaskResponse(task);
    }

    @Transactional
    public ProjectResponse assignLabelToProject(String projectId, String labelId) {
        if (!permissionService.isOwner(projectId)) {
            throw new ForbiddenException("Only the project owner can assign labels to the project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        if (!label.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Label does not belong to this project");
        }

        if (label.getType() != LabelType.PROJECT) {
            throw new BadRequestException("This label is not a project label");
        }

        if (!project.getLabels().contains(label)) {
            project.getLabels().add(label);
            projectRepository.save(project);
        }

        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse removeLabelFromProject(String projectId, String labelId) {
        if (!permissionService.isOwner(projectId)) {
            throw new ForbiddenException("Only the project owner can remove labels from the project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        project.getLabels().remove(label);
        projectRepository.save(project);
        return mapToProjectResponse(project);
    }

    private LabelResponse mapToLabelResponse(Label label) {
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .type(label.getType())
                .projectId(label.getProject().getId())
                .createdAt(label.getCreatedAt())
                .updatedAt(label.getUpdatedAt())
                .build();
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority("MEDIUM")
                .project(ProjectResponse.builder()
                        .id(task.getProject().getId())
                        .name(task.getProject().getName())
                        .description(task.getProject().getDescription())
                        .owner(UserResponse.builder()
                                .id(task.getProject().getOwner().getId())
                                .fullName(task.getProject().getOwner().getFullName())
                                .email(task.getProject().getOwner().getEmail())
                                .createdAt(task.getProject().getOwner().getCreatedAt())
                                .build())
                        .labels(task.getProject().getLabels() != null ? task.getProject().getLabels().stream()
                                .map(this::mapToLabelResponse)
                                .toList() : List.of())
                        .createdAt(task.getProject().getCreatedAt())
                        .updatedAt(null)
                        .build())
                .assignee(task.getAssignee() != null ? UserResponse.builder()
                        .id(task.getAssignee().getId())
                        .fullName(task.getAssignee().getFullName())
                        .email(task.getAssignee().getEmail())
                        .createdAt(task.getAssignee().getCreatedAt())
                        .build() : null)
                .reporter(UserResponse.builder()
                        .id(task.getCreatedBy().getId())
                        .fullName(task.getCreatedBy().getFullName())
                        .email(task.getCreatedBy().getEmail())
                        .createdAt(task.getCreatedBy().getCreatedAt())
                        .build())
                .labels(task.getLabels() != null ? task.getLabels().stream()
                        .map(this::mapToLabelResponse)
                        .toList() : List.of())
                .dueDate(null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private ProjectResponse mapToProjectResponse(Project project) {
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
                        .map(this::mapToLabelResponse)
                        .toList() : List.of())
                .createdAt(project.getCreatedAt())
                .updatedAt(null)
                .build();
    }
}