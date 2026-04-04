package com.manager.minerai.services;

import com.manager.minerai.domain.Comment;
import com.manager.minerai.domain.Task;
import com.manager.minerai.domain.User;
import com.manager.minerai.dto.request.comment.CreateCommentRequest;
import com.manager.minerai.dto.request.comment.UpdateCommentRequest;
import com.manager.minerai.dto.response.CommentResponse;
import com.manager.minerai.enums.PermissionType;
import com.manager.minerai.exception.ForbiddenException;
import com.manager.minerai.exception.ResourceNotFoundException;
import com.manager.minerai.repository.CommentRepository;
import com.manager.minerai.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final PermissionService permissionService;

    public CommentResponse createComment(String projectId, String taskId, CreateCommentRequest request) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        User author = permissionService.getCurrentUser();

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .author(author)
                .build();

        commentRepository.save(comment);
        return mapToResponse(comment);
    }

    public List<CommentResponse> getTaskComments(String projectId, String taskId) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CommentResponse updateComment(String projectId, String taskId, String commentId, UpdateCommentRequest request) {
        Comment comment = getCommentAndCheckOwnership(projectId, taskId, commentId);

        comment.setContent(request.getContent());
        commentRepository.save(comment);
        return mapToResponse(comment);
    }

    public void deleteComment(String projectId, String taskId, String commentId) {
        Comment comment = getCommentAndCheckOwnership(projectId, taskId, commentId);
        commentRepository.delete(comment);
    }

    private Comment getCommentAndCheckOwnership(String projectId, String taskId, String commentId) {
        permissionService.checkPermission(projectId, PermissionType.UPDATE_STATUS);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getTask().getId().equals(taskId)) {
            throw new ForbiddenException("Comment does not belong to this task");
        }

        if (!comment.getTask().getProject().getId().equals(projectId)) {
            throw new ForbiddenException("Task does not belong to this project");
        }

        User currentUser = permissionService.getCurrentUser();
        boolean isOwner = permissionService.isOwner(projectId);

        if (!isOwner && !comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update or delete your own comments");
        }

        return comment;
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getFullName())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
