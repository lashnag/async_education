package ru.lashnev.analytics

import java.time.LocalDateTime

data class User(
    val userPublicUid: String,
    val role: Role,
)

enum class Role {
    ADMIN, OTHER
}

data class Account(
    val accountPublicUid: String,
    val userPublicId: String,
    val balance: Long,
    val operations: MutableList<Operation>
)

data class Operation(
    val description: String,
    val dateTime: LocalDateTime,
    val changeAmount: Long,
)

data class Task(
    val taskPublicUid: String,
    val title: String,
    val donePrice: Long,
    val creationTime: LocalDateTime,
    val jiraId: String?,
)

data class PartialTaskWithoutPrice(
    val taskPublicUid: String,
    val title: String,
    val creationTime: LocalDateTime,
    val jiraId: String?,
)

data class TaskPrice(
    val taskPublicUid: String,
    val donePrice: Long,
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
    val creationTime: String,
    val metaData: ReplicationMetaData,
    val jiraId: String,
)

data class ReplicationTaskDonePriceCalculated(
    val taskPublicUid: String,
    val price: Long,
    val metaData: ReplicationMetaData,
)

data class ReplicationAccountCreated(
    val accountPublicUid: String,
    val userPublicUId: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationAccountBalanceChanged(
    val accountPublicUid: String,
    val currentBalance: Long,
    val metaData: ReplicationMetaData,
)

data class ReplicationJiraIdAddedToTask(
    val taskPublicUid: String,
    val jiraId: String,
    val metaData: ReplicationMetaData,
)

data class ReplicationOperationCreated(
    val accountPublicUid: String,
    val description: String,
    val dateTime: String,
    val changeAmount: Long,
    val metaData: ReplicationMetaData,
)
