package ru.lashnev.users

data class User(
    val login: String,
    val role: Role
)

enum class Role {
    USER, ADMIN, MANAGER, ACCOUNT
}
