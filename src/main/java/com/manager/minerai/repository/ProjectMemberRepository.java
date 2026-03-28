package com.manager.minerai.repository;

import com.manager.minerai.domain.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, String> {
    List<ProjectMember> findByProjectId(String projectId);
    Optional<ProjectMember> findByUserIdAndProjectId(String userId, String projectId);
    boolean existsByUserIdAndProjectId(String userId, String projectId);
}
