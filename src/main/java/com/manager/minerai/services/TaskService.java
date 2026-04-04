package com.manager.minerai.services;

import com.manager.minerai.domain.Project;
import com.manager.minerai.domain.Task;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.task.CreateTaskRequest;
import com.manager.minerai.dto.request.task.UpdateStatusRequest;
import com.manager.minerai.dto.request.task.UpdateTaskRequest;
import com.manager.minerai.dto.response.TaskResponse;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.ProjectRepository;
import com.manager.minerai.repository.TaskRepository;
import com.manager.minerai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    public TaskResponse createTask(String projectId, CreateTaskRequest request) {
        permissionService.checkPermission(projectId, PermissionType.CREATE_TASK);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(()-> new ResourceNotFoundException("Project not found"));

        User createdBy = permissionService.getCurrentUser();

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(()-> new ResourceNotFoundException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .assignee(assignee)
                .createdBy(createdBy)
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
                .orElseThrow(()-> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssignee(assignee);
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

    public void deleteTask(String projectId, String taskId) {
        permissionService.checkPermission(projectId, PermissionType.DELETE_TASK);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(()-> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not match to this project");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .projectId(task.getProject().getId())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .createdById(task.getCreatedBy().getId())
                .createdByName(task.getCreatedBy().getFullName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
