package com.manager.minerai.repository;

import com.manager.minerai.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    List<Permission> findByProjectRoleId(String projectRoleId);
    void deleteByProjectRoleId(String projectRoleId);
}
