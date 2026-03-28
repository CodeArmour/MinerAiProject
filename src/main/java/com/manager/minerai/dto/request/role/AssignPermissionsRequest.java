package com.manager.minerai.dto.request.role;

import com.manager.minerai.enums.PermissionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequest {
    @NotNull(message = "Permissions list is required")
    @NotEmpty(message = "Permissions list cannot be empty")
    private List<PermissionType> permissions;
}
