package com.manager.minerai.dto.response;

import com.manager.minerai.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private String priority;
    private ProjectResponse project;
    private UserResponse assignee;
    private UserResponse reporter;
    private List<LabelResponse> labels;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
