package com.manager.minerai.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_roles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "projectRole", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void updatePermissions(List<Permission> newPermissions) {
        this.permissions.clear();
        this.permissions.addAll(newPermissions);
    }
}