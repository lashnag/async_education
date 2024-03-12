package ru.lashnev.task_manager

import java.util.Date
import java.util.UUID

fun ReplicationUser.toUser(): User {
    return User(
        publicUid = this.publicUserUid,
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

fun Task.toReplicationTask(eventVersion: String, producer: String): ReplicationCreateTask {
    return ReplicationCreateTask(
        taskPublicUid = this.taskPublicUid.toString(),
        authorPublicUid = this.authorPublicUid,
        jiraId = jiraId,
        title = this.description,
        creationTime = this.creationTime.toString(),
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        )
    )
}

fun Task.toReplicationAssignedTask(eventVersion: String, producer: String): ReplicationAssignedTask {
    return ReplicationAssignedTask(
        taskPublicUid = this.taskPublicUid.toString(),
        assignedUserPublicUid = this.assignedUserPublicUid,
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        )
    )
}

fun Task.toReplicationClosedTask(eventVersion: String, producer: String): ReplicationClosedTask {
    return ReplicationClosedTask(
        taskPublicUid = this.taskPublicUid.toString(),
        metaData = ReplicationMetaData(
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            eventVersion = eventVersion,
            producer = producer,
        )
    )
}
