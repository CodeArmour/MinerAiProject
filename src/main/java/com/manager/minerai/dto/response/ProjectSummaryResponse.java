package com.manager.minerai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryResponse {
    private String id;
    private String name;
    private String description;
    private long membersCount;
    private long tasksCount;
}
