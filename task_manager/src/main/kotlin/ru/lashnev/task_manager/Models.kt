package ru.lashnev.task_manager

import java.time.LocalDateTime
import java.util.UUID

data class Task(
    val id: Int,
    val taskPublicUid: UUID,
    val authorPublicUid: String,
    val title: String,
    val creationTime: LocalDateTime = LocalDateTime.now(),
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

data class ReplicationCreateTask(
    val taskPublicUid: String,
    val authorPublicUid: String,
    val title: String,
    val creationTime: String,
    val jiraId: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationClosedTask(
    val taskPublicUid: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationAssignedTask(
    val taskPublicUid: String,
    val assignedUserPublicUid: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationUser(
    val publicUserUid: String,
    val role: ReplicationRole,
    val metaData: ReplicationMetaData,
)

enum class ReplicationRole {
    USER, ADMIN, MANAGER, ACCOUNT
}

data class ReplicationMetaData(
    val eventVersion: String,
    val eventId: String,
    val eventTime: String,
    val producer: String,
)
