package com.manager.minerai.services;

import com.manager.minerai.domain.Label;
import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.Task;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.task.CreateTaskRequest;
import com.manager.minerai.dto.request.task.UpdatePriorityRequest;
import com.manager.minerai.dto.request.task.UpdateStatusRequest;
import com.manager.minerai.dto.request.task.UpdateTaskRequest;
import com.manager.minerai.dto.response.LabelResponse;
import com.manager.minerai.dto.response.ProjectResponse;
import com.manager.minerai.dto.response.TaskResponse;
import com.manager.minerai.dto.response.UserResponse;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.enums.Priority;
import com.manager.minerai.enums.TaskStatus;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.LabelRepository;
import com.manager.minerai.repository.ProjectRepository;
import com.manager.minerai.repository.TaskRepository;
import com.manager.minerai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.manager.minerai.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final LabelRepository labelRepository;

    public TaskResponse createTask(String projectId, CreateTaskRequest request) {
        permissionService.checkPermission(projectId, PermissionType.CREATE_TASK);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User createdBy = permissionService.getCurrentUser();

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        List<Label> labels = new ArrayList<>();
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            labels = labelRepository.findAllById(request.getLabelIds());
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.NEW)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(project)
                .assignee(assignee)
                .createdBy(createdBy)
                .labels(labels)
                .build();

        taskRepository.save(task);
        return mapToResponse(task);
    }

    public List<TaskResponse> getProjectTasks(String projectId) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(String projectId, String taskId) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found"));
        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }
        return mapToResponse(task);
    }

    public TaskResponse updateTask(String projectId, String taskId, UpdateTaskRequest request) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_TASK);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        } else {
            assignee = task.getAssignee();
        }

        List<Label> labels;
        if (request.getLabelIds() != null) {
            labels = request.getLabelIds().isEmpty()
                    ? new ArrayList<>()
                    : labelRepository.findAllById(request.getLabelIds());
        } else {
            labels = task.getLabels();
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setDueDate(request.getDueDate());
        task.setAssignee(assignee);
        task.setLabels(labels);

        taskRepository.save(task);
        return mapToResponse(task);
    }

    public TaskResponse updateStatus(String projectId, String taskId, UpdateStatusRequest request) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not match to this project");
        }

        task.setStatus(request.getStatus());
        taskRepository.save(task);
        return mapToResponse(task);
    }

    public TaskResponse updatePriority(String projectId, String taskId, UpdatePriorityRequest request) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_PRIORITY);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found"));
        task.setPriority(request.getPriority());
        taskRepository.save(task);
        return mapToResponse(task);
    }

    public PageResponse<TaskResponse> getProjectTasksPaginated(String projectId,
                                                               int page,
                                                               int size) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);

        return PageResponse.<TaskResponse>builder()
                .content(taskPage.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(taskPage.getNumber())
                .pageSize(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    public void deleteTask(String projectId, String taskId) {
        permissionService.checkPermission(projectId, PermissionType.DELETE_TASK);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not match to this project");
        }

        taskRepository.delete(task);
    }

    public PageResponse<TaskResponse> searchTasks(String projectId,
                                                  TaskStatus status,
                                                  String assigneeId,
                                                  String keyword,
                                                  int page,
                                                  int size) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);

        Specification<Task> spec = Specification.where(null);

        spec = spec.and((root, query, cb) -> cb.equal(root.get("project").get("id"), projectId));

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (assigneeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return PageResponse.<TaskResponse>builder()
                .content(taskPage.getContent().stream().map(this::mapToResponse).toList())
                .pageNumber(taskPage.getNumber())
                .pageSize(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(String.valueOf(task.getPriority()))
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
                                .map(label -> LabelResponse.builder()
                                        .id(label.getId())
                                        .name(label.getName())
                                        .color(label.getColor())
                                        .type(label.getType())
                                        .projectId(task.getProject().getId())
                                        .createdAt(label.getCreatedAt())
                                        .updatedAt(label.getUpdatedAt())
                                        .build())
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
                        .map(label -> LabelResponse.builder()
                                .id(label.getId())
                                .name(label.getName())
                                .color(label.getColor())
                                .type(label.getType())
                                .projectId(label.getProject().getId())
                                .createdAt(label.getCreatedAt())
                                .updatedAt(label.getUpdatedAt())
                                .build())
                        .toList() : List.of())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
