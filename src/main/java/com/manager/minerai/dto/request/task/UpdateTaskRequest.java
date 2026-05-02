package com.manager.minerai.dto.request.task;

import com.manager.minerai.enums.Priority;
import com.manager.minerai.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateTaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private String assigneeId;

    private TaskStatus status;

    private Priority priority;

    private LocalDate dueDate;

    private List<String> labelIds;
}