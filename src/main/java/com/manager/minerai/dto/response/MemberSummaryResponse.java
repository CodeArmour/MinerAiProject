package com.manager.minerai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryResponse {
    private String fullName;
    private String email;
    private long totalProjectsAsMember;
    private long totalAssignedTasks;
    private List<MemberProjectResponse> projects;
    private List<AssignedTaskResponse> assignedTasks;
}