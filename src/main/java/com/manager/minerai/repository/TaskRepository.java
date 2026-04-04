package com.manager.minerai.repository;

import com.manager.minerai.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByProjectId(String projectId);
    List<Task> findByAssigneeId(String assigneeId);
}
