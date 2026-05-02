package com.manager.minerai.controllers;

import com.manager.minerai.dto.request.comment.CreateCommentRequest;
import com.manager.minerai.dto.request.comment.UpdateCommentRequest;
import com.manager.minerai.dto.response.CommentResponse;
import com.manager.minerai.dto.response.PageResponse;
import com.manager.minerai.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable String projectId,
                                                          @PathVariable String taskId,
                                                          @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(projectId, taskId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getTaskComments(@PathVariable String projectId,
                                                                  @PathVariable String taskId) {
        return ResponseEntity.ok(commentService.getTaskComments(projectId, taskId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<CommentResponse>> getTaskCommentsPaginated(
            @PathVariable String projectId,
            @PathVariable String taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getTaskCommentsPaginated(projectId, taskId, page, size));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable String projectId,
                                                          @PathVariable String taskId,
                                                          @PathVariable String commentId,
                                                          @Valid @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(projectId, taskId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String projectId,
                                               @PathVariable String taskId,
                                               @PathVariable String commentId) {
        commentService.deleteComment(projectId, taskId, commentId);
        return ResponseEntity.noContent().build();
    }
}
