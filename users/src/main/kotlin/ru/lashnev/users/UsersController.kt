package ru.lashnev.users

import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Сделано чтобы не подключать БД. Будем считать что эти ручки доступны только супер админам
 */

@RestController
class UsersController(
    private val userDetailManager: UserDetailsService,
    private val eventProducer: EventProducer,
) {

    @PutMapping("/admin/create_user")
    fun addUser(login: String, password: String): ResponseEntity<String> {
        val user = User.builder()
            .username(login)
            .password(password)
            .passwordEncoder(encoder::encode)
            .roles(Roles.USER.name)
            .build()
        (userDetailManager as InMemoryUserDetailsManager).createUser(user)
        eventProducer.addUser(user)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/admin/change_user_role")
    fun changeUserRole(login: String, roles: Roles): ResponseEntity<String> {
        val user = User.builder()
            .username(login)
            .roles(roles.name)
            .build()
        (userDetailManager as InMemoryUserDetailsManager).updateUser(user)
        eventProducer.changeUserRole(user)
        return ResponseEntity.accepted().build()
    }

    companion object {
        val encoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}
