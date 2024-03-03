package ru.lashnev.users

data class ReplicationUser(
    val login: String,
    val role: ReplicationRole
)

enum class ReplicationRole {
    USER, ADMIN, MANAGER, ACCOUNT
}
