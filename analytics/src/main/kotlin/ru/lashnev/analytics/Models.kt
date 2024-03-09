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
)

data class Task(
    val taskPublicUid: String,
    val title: String,
    val donePrice: Long? = null,
    val creationDate: LocalDateTime
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

data class ReplicationCreateTask(
    val taskPublicUid: String,
    val authorPublicUid: String,
    val title: String,
    val jiraId: String? = null,
    val metaData: ReplicationMetaData,
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
