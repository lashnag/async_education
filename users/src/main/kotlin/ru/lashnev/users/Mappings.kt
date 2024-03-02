package ru.lashnev.users

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

fun UserDetails.toUser(): User {
    return User(login = this.username, role = this.authorities.first().toRole())
}

fun GrantedAuthority.toRole(): Role {
    return Role.USER
}
