package com.manager.minerai.repository;

import com.manager.minerai.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(String taskId);
}
