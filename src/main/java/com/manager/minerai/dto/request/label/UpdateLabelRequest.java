package com.manager.minerai.dto.request.label;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLabelRequest {

    @NotBlank(message = "Label name is required")
    private String name;

    @NotBlank(message = "Label color is required")
    private String color;
}
