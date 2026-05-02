package com.manager.minerai.dto.response;

import com.manager.minerai.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private String id;
    private String email;
    private String token;
    private InvitationStatus status;
    private String projectId;
    private String projectName;
    private String invitedByName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
}
