package ru.lashnev.task_manager

fun ReplicationUser.toUser(): User {
    return User(
        login = this.login,
        role = this.role.toRole(),
    )
}

fun ReplicationRole.toRole(): Role {
    return when(this) {
        ReplicationRole.USER -> Role.OTHER
        ReplicationRole.ACCOUNT -> Role.OTHER
        ReplicationRole.ADMIN -> Role.ADMIN
        ReplicationRole.MANAGER -> Role.MANAGER
    }
}

fun Task.toReplicationTask(): ReplicationTask {
    return ReplicationTask(
        this.uuid,
        this.author,
        this.description,
        this.assignedUser,
    )
}
