package com.manager.minerai.controllers;

import com.manager.minerai.dto.request.label.CreateLabelRequest;
import com.manager.minerai.dto.request.label.UpdateLabelRequest;
import com.manager.minerai.dto.response.LabelResponse;
import com.manager.minerai.dto.response.ProjectResponse;
import com.manager.minerai.dto.response.TaskResponse;
import com.manager.minerai.enums.LabelType;
import com.manager.minerai.services.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(@PathVariable String projectId,
                                                     @Valid @RequestBody CreateLabelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(labelService.createLabel(projectId, request));
    }

    @GetMapping
    public ResponseEntity<List<LabelResponse>> getProjectLabels(@PathVariable String projectId) {
        return ResponseEntity.ok(labelService.getProjectLabels(projectId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<LabelResponse>> getLabelsByType(@PathVariable String projectId,
                                                               @RequestParam LabelType type) {
        return ResponseEntity.ok(labelService.getProjectLabelsByType(projectId, type));
    }

    @PutMapping("/{labelId}")
    public ResponseEntity<LabelResponse> updateLabel(@PathVariable String projectId,
                                                     @PathVariable String labelId,
                                                     @Valid @RequestBody UpdateLabelRequest request) {
        return ResponseEntity.ok(labelService.updateLabel(projectId, labelId, request));
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(@PathVariable String projectId,
                                            @PathVariable String labelId) {
        labelService.deleteLabel(projectId, labelId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<TaskResponse> assignLabelToTask(@PathVariable String projectId,
                                                          @PathVariable String taskId,
                                                          @PathVariable String labelId) {
        return ResponseEntity.ok(labelService.assignLabelToTask(projectId, taskId, labelId));
    }

    @DeleteMapping("/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<TaskResponse> removeLabelFromTask(@PathVariable String projectId,
                                                            @PathVariable String taskId,
                                                            @PathVariable String labelId) {
        return ResponseEntity.ok(labelService.removeLabelFromTask(projectId, taskId, labelId));
    }

    // project label assignment
    @PostMapping("/assign/{labelId}")
    public ResponseEntity<ProjectResponse> assignLabelToProject(@PathVariable String projectId,
                                                                @PathVariable String labelId) {
        return ResponseEntity.ok(labelService.assignLabelToProject(projectId, labelId));
    }

    @DeleteMapping("/assign/{labelId}")
    public ResponseEntity<ProjectResponse> removeLabelFromProject(@PathVariable String projectId,
                                                                  @PathVariable String labelId) {
        return ResponseEntity.ok(labelService.removeLabelFromProject(projectId, labelId));
    }
}
