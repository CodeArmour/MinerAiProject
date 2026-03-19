package com.manager.minerai.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.logging.log4j.message.Message;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
}
