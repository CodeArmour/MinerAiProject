package com.manager.minerai.dto.request.task;

import com.manager.minerai.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePriorityRequest {
    @NotNull(message = "Priority is required")
    private Priority priority;
}
