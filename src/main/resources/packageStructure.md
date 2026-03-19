# Project Manager — Spring Boot Project Structure

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (Access + Refresh tokens) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Mapping | MapStruct |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Package Structure

```
com.yourapp.projectmanager
│
├── config/
│   ├── AppConfig.java                  # Beans: PasswordEncoder, ModelMapper
│   ├── JwtConfig.java                  # JWT secret, expiry properties
│   └── OpenApiConfig.java              # Swagger / OpenAPI setup
│
├── security/
│   ├── JwtUtil.java                    # Generate / validate / parse tokens
│   ├── JwtAuthFilter.java              # OncePerRequestFilter — validates Bearer token
│   ├── CustomUserDetailsService.java   # Loads user from DB by email
│   ├── SecurityConfig.java             # SecurityFilterChain, CORS, public routes
│   └── AuthEntryPoint.java             # 401 Unauthorized handler
│
├── domain/                             # JPA Entities — pure data, no business logic
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   ├── Comment.java
│   ├── ProjectMember.java              # Join: user ↔ project + assigned role
│   ├── ProjectRole.java                # Custom role scoped to a project
│   └── Permission.java                 # Maps role → list of PermissionType
│
├── enums/
│   ├── TaskStatus.java                 # NEW | IN_PROGRESS | IN_TEST | COMPLETED
│   └── PermissionType.java             # Fine-grained permissions (see below)
│
├── repository/
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   ├── CommentRepository.java
│   ├── ProjectMemberRepository.java
│   ├── ProjectRoleRepository.java
│   └── PermissionRepository.java
│
├── dto/
│   ├── request/
│   │   ├── auth/
│   │   │   ├── RegisterRequest.java
│   │   │   └── LoginRequest.java
│   │   ├── project/
│   │   │   ├── CreateProjectRequest.java
│   │   │   └── UpdateProjectRequest.java
│   │   ├── task/
│   │   │   ├── CreateTaskRequest.java
│   │   │   ├── UpdateTaskRequest.java
│   │   │   └── UpdateStatusRequest.java
│   │   ├── comment/
│   │   │   ├── CreateCommentRequest.java
│   │   │   └── UpdateCommentRequest.java
│   │   ├── member/
│   │   │   └── AddMemberRequest.java
│   │   └── role/
│   │       ├── CreateRoleRequest.java
│   │       └── AssignPermissionsRequest.java
│   └── response/
│       ├── AuthResponse.java           # accessToken + refreshToken
│       ├── ProjectResponse.java
│       ├── TaskResponse.java
│       ├── CommentResponse.java
│       └── UserSummaryResponse.java
│
├── service/
│   ├── AuthService.java
│   ├── ProjectService.java
│   ├── TaskService.java
│   ├── CommentService.java
│   ├── MemberService.java
│   ├── RoleService.java
│   └── PermissionService.java          # Core: hasPermission(userId, projectId, PermissionType)
│
├── controller/
│   ├── AuthController.java             # POST /api/auth/register, /login, /refresh
│   ├── ProjectController.java          # /api/projects/**
│   ├── TaskController.java             # /api/projects/{id}/tasks/**
│   ├── CommentController.java          # /api/tasks/{id}/comments/**
│   ├── MemberController.java           # /api/projects/{id}/members/**
│   └── RoleController.java             # /api/projects/{id}/roles/**
│
├── exception/
│   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   ├── ResourceNotFoundException.java  # 404
│   ├── UnauthorizedException.java      # 401
│   └── ForbiddenException.java         # 403
│
└── mapper/
    ├── ProjectMapper.java
    ├── TaskMapper.java
    └── UserMapper.java
```

---

## Domain Model (Entities)

### User
```
id, email, password (hashed), fullName, createdAt
```

### Project
```
id, name, description, owner (User), createdAt
```

### ProjectRole  *(scoped per project — created by owner)*
```
id, project (Project), name (e.g. "Dev", "QA", "Lead")
→ permissions: List<Permission>
```

### Permission
```
id, projectRole (ProjectRole), permissionType (PermissionType)
```

### ProjectMember  *(join: user ↔ project with a role)*
```
id, user (User), project (Project), projectRole (ProjectRole), joinedAt
```
> A user can be in multiple projects each with a different role.

### Task
```
id, project (Project), title, description,
assignee (User), status (TaskStatus),
createdBy (User), createdAt, updatedAt
```

### Comment
```
id, task (Task), author (User), content, createdAt, updatedAt
```

---

## Enums

### TaskStatus
```java
NEW, IN_PROGRESS, IN_TEST, COMPLETED
```

### PermissionType
```java
// Task permissions
CREATE_TASK,
DELETE_TASK,
ASSIGN_TASK,
UPDATE_TASK,
UPDATE_STATUS,

// Member permissions
ADD_MEMBER,
REMOVE_MEMBER,

// Comment permissions
WRITE_COMMENT,
UPDATE_COMMENT,
DELETE_COMMENT,

// Role management (owner only — enforced separately)
MANAGE_ROLES
```

---

## API Endpoints Overview

### Auth — `/api/auth`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Register new user |
| POST | `/login` | Login — returns access + refresh tokens |
| POST | `/refresh` | Rotate access token using refresh token |
| POST | `/logout` | Invalidate refresh token |

### Projects — `/api/projects`
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | List all projects for current user |
| POST | `/` | Create new project |
| GET | `/{id}` | Get project details |
| PUT | `/{id}` | Update project |
| DELETE | `/{id}` | Delete project (owner only) |

### Tasks — `/api/projects/{projectId}/tasks`
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | List tasks in project |
| POST | `/` | Create task (requires CREATE_TASK) |
| GET | `/{taskId}` | Get task details |
| PUT | `/{taskId}` | Update task (requires UPDATE_TASK) |
| PATCH | `/{taskId}/status` | Update status (requires UPDATE_STATUS) |
| DELETE | `/{taskId}` | Delete task (requires DELETE_TASK) |

### Comments — `/api/tasks/{taskId}/comments`
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | List comments on task |
| POST | `/` | Add comment (requires WRITE_COMMENT) |
| PUT | `/{commentId}` | Edit comment (requires UPDATE_COMMENT) |
| DELETE | `/{commentId}` | Delete comment (requires DELETE_COMMENT) |

### Members — `/api/projects/{projectId}/members`
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | List members |
| POST | `/` | Add member (requires ADD_MEMBER) |
| PUT | `/{memberId}/role` | Change member role |
| DELETE | `/{memberId}` | Remove member (requires REMOVE_MEMBER) |

### Roles — `/api/projects/{projectId}/roles`
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | List roles in project |
| POST | `/` | Create role (owner only) |
| PUT | `/{roleId}/permissions` | Assign permissions to role (owner only) |
| DELETE | `/{roleId}` | Delete role (owner only) |

---

## Authorization Flow

```
Request hits JwtAuthFilter
    → extract userId from token
    → load user from DB

Controller calls Service
    → Service calls PermissionService.hasPermission(userId, projectId, PermissionType.X)
        → look up ProjectMember by (userId, projectId)
        → get ProjectRole → get Permission list
        → check if PermissionType.X is present
        → throw ForbiddenException if not

Project owner bypasses permission checks (checked via project.owner.id == userId)
```

---

## Maven Dependencies (`pom.xml`)

```xml
<!-- Web -->
<dependency>spring-boot-starter-web</dependency>

<!-- Data -->
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>postgresql</dependency>

<!-- Security -->
<dependency>spring-boot-starter-security</dependency>
<dependency>jjwt-api</dependency>   <!-- 0.12.x -->
<dependency>jjwt-impl</dependency>
<dependency>jjwt-jackson</dependency>

<!-- Validation -->
<dependency>spring-boot-starter-validation</dependency>

<!-- Utilities -->
<dependency>lombok</dependency>
<dependency>mapstruct</dependency>

<!-- Docs -->
<dependency>springdoc-openapi-starter-webmvc-ui</dependency>

<!-- Test -->
<dependency>spring-boot-starter-test</dependency>
<dependency>spring-security-test</dependency>
```

---

## Development Order (Recommended)

1. **Domain entities + enums** — foundation everything else builds on
2. **Repositories** — no logic, just JPA interfaces
3. **Auth** — `User`, `JwtUtil`, `SecurityConfig`, `AuthService`, `AuthController`
4. **PermissionService** — core of the whole access control system
5. **Project CRUD** — `ProjectService` + `ProjectController`
6. **Roles & Members** — `RoleService`, `MemberService`, their controllers
7. **Task CRUD + status updates** — guarded by `PermissionService`
8. **Comments** — simplest feature, saved for last
9. **Exception handling** — `GlobalExceptionHandler` (do this early, refine late)
10. **DTOs + Mappers** — wire in as each feature is built

---

> **AI Integration (future MVC)** — `PromptController` + `AiProjectService` will sit alongside the existing services and use the same domain model. No structural changes needed — just new classes on top.