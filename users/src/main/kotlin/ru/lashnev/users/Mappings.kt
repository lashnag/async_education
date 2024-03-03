package ru.lashnev.users

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

fun UserDetails.toReplicationUser(): ReplicationUser {
    return ReplicationUser(login = this.username, role = this.authorities.first().toReplicationRole())
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
