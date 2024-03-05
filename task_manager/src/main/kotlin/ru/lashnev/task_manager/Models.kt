package ru.lashnev.task_manager

import java.util.UUID

data class Task(
    val id: Int,
    val publicUid: UUID,
    val authorPublicUid: String,
    val description: String,
    val assignedUserPublicUid: String,
    val status: TaskStatus = TaskStatus.OPEN,
)

enum class TaskStatus {
    OPEN, CLOSED
}

data class User(
    val publicUid: String,
    val role: Role,
)

enum class Role {
    ADMIN, MANAGER, OTHER
}

data class ReplicationTask(
    val taskPublicUid: UUID,
    val authorPublicUid: String,
    val description: String,
)

data class ReplicationClosedTask(
    val taskPublicUid: UUID,
)

data class ReplicationAssignedTask(
    val taskPublicUid: UUID,
    val assignedUserPublicUid: String,
)

data class ReplicationUser(
    val login: String,
    val role: ReplicationRole
)

enum class ReplicationRole {
    USER, ADMIN, MANAGER, ACCOUNT
}
