package com.manager.minerai.controllers;


import com.manager.minerai.dto.request.project.CreateProjectRequest;
import com.manager.minerai.dto.request.project.UpdateProjectRequest;
import com.manager.minerai.dto.response.ProjectResponse;
import com.manager.minerai.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

     private final ProjectService projectService;

     @PostMapping
     public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
         return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
     }

     @GetMapping
     public ResponseEntity<List<ProjectResponse>> getMyProjects() {
         return ResponseEntity.ok(projectService.getMyProjects());
     }

     @GetMapping("/{projectId}")
     public ResponseEntity<ProjectResponse> getById(@PathVariable String projectId) {
         return ResponseEntity.ok(projectService.getById(projectId));
     }

     @PutMapping("/{projectId}")
     public ResponseEntity<ProjectResponse> update(@PathVariable String projectId, @Valid @RequestBody UpdateProjectRequest request) {
         return ResponseEntity.ok(projectService.update(projectId, request));
     }

     @DeleteMapping("/{projectId}")
     public ResponseEntity<Void> delete(@PathVariable String projectId) {
         projectService.delete(projectId);
         return ResponseEntity.noContent().build();
     }



}
