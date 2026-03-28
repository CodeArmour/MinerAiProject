package com.manager.minerai.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateMemberRoleRequest {

    @NotBlank(message = "Role id is required")
    private String roleId;
}
