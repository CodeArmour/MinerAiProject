package com.manager.minerai.controllers;

import com.manager.minerai.dto.response.MemberProjectResponse;
import com.manager.minerai.dto.response.MemberSummaryResponse;
import com.manager.minerai.dto.response.OwnerSummaryResponse;
import com.manager.minerai.dto.response.PageResponse;
import com.manager.minerai.dto.response.ProjectResponse;
import com.manager.minerai.dto.response.UserResponse;
import com.manager.minerai.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @GetMapping("/me/owner-summary")
    public ResponseEntity<OwnerSummaryResponse> getOwnerSummary() {
        return ResponseEntity.ok(userService.getOwnerSummary());
    }

    @GetMapping("/me/member-summary")
    public ResponseEntity<MemberSummaryResponse> getMemberSummary() {
        return ResponseEntity.ok(userService.getMemberSummary());
    }

    @GetMapping("/me/owned-projects")
    public ResponseEntity<PageResponse<ProjectResponse>> getOwnedProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getOwnedProjects(page, size));
    }

    @GetMapping("/me/member-projects")
    public ResponseEntity<PageResponse<MemberProjectResponse>> getMemberProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getMemberProjects(page, size));
    }
}