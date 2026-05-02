package com.manager.minerai.dto.response;

import com.manager.minerai.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedTaskResponse {
    private String id;
    private String title;
    private TaskStatus status;
    private String projectId;
    private String projectName;
}
