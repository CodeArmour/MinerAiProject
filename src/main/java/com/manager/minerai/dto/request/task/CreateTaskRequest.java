package com.manager.minerai.dto.request.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private String assigneeId;
}
