package com.manager.minerai.controllers;

import com.manager.minerai.dto.request.task.CreateTaskRequest;
import com.manager.minerai.dto.request.task.UpdatePriorityRequest;
import com.manager.minerai.dto.request.task.UpdateStatusRequest;
import com.manager.minerai.dto.request.task.UpdateTaskRequest;
import com.manager.minerai.dto.response.PageResponse;
import com.manager.minerai.dto.response.TaskResponse;
import com.manager.minerai.enums.TaskStatus;
import com.manager.minerai.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable String projectId,
                                                    @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getProjectTasks(@PathVariable String projectId) {
        return ResponseEntity.ok(taskService.getProjectTasks(projectId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String projectId,
                                                     @PathVariable String taskId) {
        return ResponseEntity.ok(taskService.getTaskById(projectId, taskId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<TaskResponse>> getProjectTasksPaginated(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getProjectTasksPaginated(projectId, page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<TaskResponse>> searchTasks(
            @PathVariable String projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String assigneeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.searchTasks(projectId, status, assigneeId, keyword, page, size));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable String projectId,
                                                    @PathVariable String taskId,
                                                    @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable String projectId,
                                                      @PathVariable String taskId,
                                                      @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(taskService.updateStatus(projectId, taskId, request));
    }

    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<TaskResponse> updatePriority(@PathVariable String projectId,
                                                       @PathVariable String taskId,
                                                       @Valid @RequestBody UpdatePriorityRequest request) {
        return ResponseEntity.ok(taskService.updatePriority(projectId, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String projectId,
                                            @PathVariable String taskId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}
