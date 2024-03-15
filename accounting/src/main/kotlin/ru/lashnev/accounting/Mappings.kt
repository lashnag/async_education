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

fun ReplicationCreateTaskV1.toTask(): Task {
    return Task(
        taskPublicUid = this.taskPublicUid,
        title = title,
        taskStatus = TaskStatus.OPEN,
        jiraId = null,
    )
}

fun ReplicationCreateTaskV2.toTask(): Task {
    return Task(
        taskPublicUid = this.taskPublicUid,
        title = this.title,
        taskStatus = TaskStatus.OPEN,
        jiraId = this.jiraId,
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

fun Operation.toReplicationOperation(accountPublicUId: String, eventVersion: String, producer: String): ReplicationOperation {
    return ReplicationOperation(
        accountPublicUid = accountPublicUId,
        description = this.description,
        changeAmount = this.changeAmount,
        dateTime = this.dateTime.toString(),
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        ),
    )
}

fun RepeatReplicationAssignedTask.toReplicationAssignedTask(producer: String): ReplicationAssignedTask {
    return ReplicationAssignedTask(
        assignedUserPublicUid = this.assignedUserPublicUid,
        taskPublicUid = this.taskPublicUid,
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = this.metaData.eventVersion,
            producer = producer,
        ),
    )
}

fun ReplicationAssignedTask.toRepeatReplicationAssignedTask(repeatCount: Int, producer: String): RepeatReplicationAssignedTask {
    return RepeatReplicationAssignedTask(
        repeatCount = repeatCount,
        assignedUserPublicUid = this.assignedUserPublicUid,
        taskPublicUid = this.taskPublicUid,
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = this.metaData.eventVersion,
            producer = producer,
        ),
    )
}
