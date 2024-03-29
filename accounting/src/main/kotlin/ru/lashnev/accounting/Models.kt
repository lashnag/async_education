package ru.lashnev.accounting

import java.time.LocalDateTime
import java.util.*

data class Account(
    val id: String,
    val accountPublicUid: UUID,
    val userPublicId: String,
    val balance: Long,
    val operations: MutableList<Operation>
)

data class Operation(
    val description: String,
    val dateTime: LocalDateTime,
    val changeAmount: Long,
)

data class User(
    val userPublicUid: String,
    val role: Role,
)

enum class Role {
    ADMIN, ACCOUNT, OTHER
}

data class Task(
    val taskPublicUid: String,
    val title: String,
    val assignedUserPublicUid: String? = null,
    val assignedPrice: Long? = null,
    val donePrice: Long? = null,
    val taskStatus: TaskStatus,
    val jiraId: String?,
)

enum class TaskStatus {
    OPEN, ASSIGNED, CLOSED
}

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

data class ReplicationCreateTaskV1(
    val taskPublicUid: String,
    val authorPublicUid: String,
    val title: String,
    val creationTime: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationCreateTaskV2(
    val taskPublicUid: String,
    val authorPublicUid: String,
    val title: String,
    val jiraId: String,
    val creationTime: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationAssignedTask(
    val taskPublicUid: String,
    val assignedUserPublicUid: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationClosedTask(
    val taskPublicUid: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationAccountCreated(
    val accountPublicUid: UUID,
    val userPublicUId: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationAccountBalanceChanged(
    val accountPublicUid: UUID,
    val currentBalance: Long,
    val metaData: ReplicationMetaData,
)

data class ReplicationTaskDonePriceCalculated(
    val taskPublicUid: String,
    val price: Long,
    val metaData: ReplicationMetaData,
)

data class ReplicationJiraIdAddedToTask(
    val taskPublicUid: String,
    val jiraId: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationOperation(
    val accountPublicUid: String,
    val description: String,
    val dateTime: String,
    val changeAmount: Long,
    val metaData: ReplicationMetaData,
)

data class RepeatReplicationAssignedTask(
    val repeatCount: Int,
    val taskPublicUid: String,
    val assignedUserPublicUid: String,
    val metaData: ReplicationMetaData,
)
