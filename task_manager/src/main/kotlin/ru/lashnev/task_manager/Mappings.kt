package ru.lashnev.task_manager

fun ReplicationUser.toUser(): User {
    return User(
        publicUid = this.login,
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
        this.publicUid,
        this.authorPublicUid,
        this.description,
    )
}

fun Task.toReplicationAssignedTask(): ReplicationAssignedTask {
    return ReplicationAssignedTask(this.publicUid, this.assignedUserPublicUid)
}

fun Task.toReplicationClosedTask(): ReplicationClosedTask {
    return ReplicationClosedTask(this.publicUid)
}
