package com.manager.minerai.repository;

import com.manager.minerai.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    List<Project> findByOwnerId(String ownerId);
    long countByOwnerId(String ownerId);
}
