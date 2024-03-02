package ru.lashnev.task_manager

import java.util.UUID

data class Task(
    val uuid: UUID,
    val author: String,
    val description: String,
    val assignedUser: String,
    val status: TaskStatus = TaskStatus.OPEN,
)

enum class TaskStatus {
    OPEN, CLOSED
}

data class User(
    val login: String,
    val role: Role,
)

enum class Role {
    ADMIN, MANAGER, OTHER
}

data class ReplicationUser(
    val login: String,
    val role: ReplicationRole
)

enum class ReplicationRole {
    USER, ADMIN, MANAGER, ACCOUNT
}
