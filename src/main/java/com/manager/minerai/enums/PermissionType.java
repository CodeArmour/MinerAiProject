package com.manager.minerai.enums;

public enum PermissionType {
    // Task permissions
    CREATE_TASK,
    DELETE_TASK,
    ASSIGN_TASK,
    UPDATE_TASK,
    UPDATE_STATUS,
    UPDATE_PRIORITY,

    // Member permissions
    ADD_MEMBER,
    REMOVE_MEMBER,

    // Comment permissions
    WRITE_COMMENT,
    UPDATE_COMMENT,
    DELETE_COMMENT,

    // Role management
    MANAGE_ROLES
}