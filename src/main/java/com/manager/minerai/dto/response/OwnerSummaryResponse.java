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
public class OwnerSummaryResponse {
    private String fullName;
    private String email;
    private long totalProjectsOwned;
    private List<ProjectSummaryResponse> projects;
}
