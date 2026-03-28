package com.manager.minerai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String id;
    private String userId;
    private String userFullName;
    private String userEmail;
    private String projectId;
    private String roleId;
    private String roleName;
    private LocalDateTime joinedAt;
}
