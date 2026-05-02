package com.manager.minerai.repository;

import com.manager.minerai.domain.Task;
import com.manager.minerai.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {
    Page<Task> findByProjectId(String projectId, Pageable pageable);
    List<Task> findByProjectId(String projectId);
    List<Task> findByAssigneeId(String assigneeId);
    List<Task> findByAssigneeIdAndProjectId(String assigneeId, String projectId);
    long countByProjectId(String projectId);
}
