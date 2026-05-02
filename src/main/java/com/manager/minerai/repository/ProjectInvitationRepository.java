package com.manager.minerai.repository;

import com.manager.minerai.domain.ProjectInvitation;
import com.manager.minerai.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, String> {

    Optional<ProjectInvitation> findByToken(String token);

    List<ProjectInvitation> findByProjectIdAndStatus(String projectId, InvitationStatus status);

    List<ProjectInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    Optional<ProjectInvitation> findByProjectIdAndEmailAndStatus(
        String projectId, String email, InvitationStatus status);

    boolean existsByProjectIdAndEmailAndStatus(
        String projectId, String email, InvitationStatus status);
}
