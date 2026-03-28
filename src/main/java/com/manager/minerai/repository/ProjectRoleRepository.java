package com.manager.minerai.repository;

import com.manager.minerai.domain.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, String> {
    List<ProjectRole> findByProjectId(String projectId);
    boolean existsByNameAndProjectId(String name, String projectId);
}
