package com.manager.minerai.controllers;

import com.manager.minerai.dto.request.role.AssignPermissionsRequest;
import com.manager.minerai.dto.request.role.CreateRoleRequest;
import com.manager.minerai.dto.response.RoleResponse;
import com.manager.minerai.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponse>  createRole(@PathVariable String projectId,
                                                    @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRole(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles(@PathVariable String projectId){
        return ResponseEntity.ok(roleService.getRoles(projectId));
    }

    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<RoleResponse> assignPermissions(@PathVariable String projectId,
                                                          @PathVariable String roleId,
                                                          @Valid @RequestBody AssignPermissionsRequest request) {
        return ResponseEntity.ok(roleService.assignPermissions(projectId, roleId, request));
    }

    @DeleteMapping("{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable String projectId,
                                           @PathVariable String roleId) {
        roleService.deleteRole(projectId, roleId);
        return ResponseEntity.noContent().build();
    }
}
