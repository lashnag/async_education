package ru.lashnev.users

data class ReplicationUser(
    val userPublicUid: String,
    val role: ReplicationRole,
    val metadata: ReplicationMetaData,
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
