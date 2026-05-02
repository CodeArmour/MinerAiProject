package com.manager.minerai.dto.response;

import com.manager.minerai.enums.LabelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelResponse {
    private String id;
    private String name;
    private String color;
    private LabelType type;
    private String projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}