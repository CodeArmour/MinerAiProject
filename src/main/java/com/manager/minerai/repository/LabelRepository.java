package com.manager.minerai.repository;

import com.manager.minerai.domain.Label;
import com.manager.minerai.enums.LabelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabelRepository extends JpaRepository<Label, String> {
    List<Label> findByProjectId(String projectId);
    List<Label> findByProjectIdAndType(String projectId, LabelType type);
    boolean existsByNameAndProjectId(String name, String projectId);
}
