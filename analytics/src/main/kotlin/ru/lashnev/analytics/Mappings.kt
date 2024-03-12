package ru.lashnev.analytics

import java.time.LocalDateTime

fun ReplicationUser.toUser(): User {
    return User(
        userPublicUid = this.publicUserUid,
        role = this.role.toRole(),
    )
}

fun ReplicationRole.toRole(): Role {
    return when(this) {
        ReplicationRole.MANAGER -> Role.OTHER
        ReplicationRole.USER -> Role.OTHER
        ReplicationRole.ACCOUNT -> Role.OTHER
        ReplicationRole.ADMIN -> Role.ADMIN
    }
}

fun ReplicationCreateTask.toTask(): Task {
    return Task(
        taskPublicUid = this.taskPublicUid,
        title = title,
        creationTime = LocalDateTime.parse(this.creationTime),
    )
}

fun ReplicationAccountCreated.toAccount(): Account {
    return Account(
        accountPublicUid = this.accountPublicUid,
        balance = 0,
        userPublicId = this.userPublicUId,
    )
}
