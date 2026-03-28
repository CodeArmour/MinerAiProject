package com.manager.minerai.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotBlank(message = "User email is required")
    private String email;

    @NotBlank(message = "Role id is required")
    private String roleId;
}
