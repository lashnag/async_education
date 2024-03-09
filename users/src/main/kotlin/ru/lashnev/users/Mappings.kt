package ru.lashnev.users

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Date
import java.util.UUID

fun UserDetails.toReplicationUser(eventVersion: String, producer: String): ReplicationUser {
    return ReplicationUser(
        userPublicUid = this.username,
        role = this.authorities.first().toReplicationRole(),
        metadata = ReplicationMetaData(
            eventVersion = eventVersion,
            eventId = UUID.randomUUID().toString(),
            eventTime = Date().toString(),
            producer = producer,
        )
    )
}

fun GrantedAuthority.toReplicationRole(): ReplicationRole {
    return when(this.authority) {
        "ROLE_USER" -> ReplicationRole.USER
        "ROLE_ADMIN" -> ReplicationRole.ADMIN
        "ROLE_ACCOUNT" -> ReplicationRole.ACCOUNT
        "ROLE_MANAGER" -> ReplicationRole.MANAGER
        else -> ReplicationRole.USER
    }
}
