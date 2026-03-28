package com.manager.minerai.controllers;

import com.manager.minerai.dto.request.role.AddMemberRequest;
import com.manager.minerai.dto.request.role.UpdateMemberRoleRequest;
import com.manager.minerai.dto.response.MemberResponse;
import com.manager.minerai.services.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects/{projectId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> addMember (@PathVariable String projectId,
                                                     @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memberService.addMember(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable String projectId) {
        return ResponseEntity.ok(memberService.getMembers(projectId));
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<MemberResponse> updateMemberRole(
            @PathVariable String projectId,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request
            ) {
        return ResponseEntity.ok(memberService.updateMemberRole(projectId, memberId, request));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable String projectId,
                                             @PathVariable String memberId)
    {
        memberService.removeMember(projectId, memberId);
        return ResponseEntity.noContent().build();
    }
}
