package com.manager.minerai.dto.request.label;

import com.manager.minerai.enums.LabelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLabelRequest {

    @NotBlank(message = "Label name is required")
    private String name;

    @NotBlank(message = "Label color is required")
    private String color;

    @NotNull(message = "Label type is required")
    private LabelType type;
}