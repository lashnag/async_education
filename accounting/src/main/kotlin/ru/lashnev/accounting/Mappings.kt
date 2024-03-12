package ru.lashnev.accounting

import java.util.*

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
        ReplicationRole.ACCOUNT -> Role.ACCOUNT
        ReplicationRole.ADMIN -> Role.ADMIN
    }
}

fun ReplicationCreateTask.toTask(): Task {
    return Task(
        taskPublicUid = this.taskPublicUid,
        title = title,
        taskStatus = TaskStatus.OPEN,
    )
}

fun Account.toReplicationAccountCreated(eventVersion: String, producer: String): ReplicationAccountCreated {
    return ReplicationAccountCreated(
        accountPublicUid = this.accountPublicUid,
        userPublicUId = this.userPublicId,
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        ),
    )
}

fun Account.toReplicationAccountBalanceChanged(eventVersion: String, producer: String): ReplicationAccountBalanceChanged {
    return ReplicationAccountBalanceChanged(
        accountPublicUid = this.accountPublicUid,
        currentBalance = this.balance,
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        ),
    )
}

fun Task.toReplicationTaskDonePriceCalculated(eventVersion: String, producer: String): ReplicationTaskDonePriceCalculated {
    return ReplicationTaskDonePriceCalculated(
        taskPublicUid = this.taskPublicUid,
        price = this.donePrice!!,
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        ),
    )
}
