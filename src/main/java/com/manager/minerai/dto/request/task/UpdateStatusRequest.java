package com.manager.minerai.dto.request.task;

import com.manager.minerai.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull(message = "Status is required")
    private TaskStatus status;

}
