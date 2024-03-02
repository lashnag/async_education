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

    private val inMemoryUserDetailsManager = userDetailManager as InMemoryUserDetailsManager

    @PutMapping("/admin/create_user")
    fun addUser(login: String, password: String): ResponseEntity<String> {
        val user = User.builder()
            .username(login)
            .password(password)
            .passwordEncoder(encoder::encode)
            .roles(Role.USER.name)
            .build()
        return if(!inMemoryUserDetailsManager.userExists(login)) {
            inMemoryUserDetailsManager.createUser(user)
            eventProducer.addUser(user)
            ResponseEntity.accepted().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/admin/change_user_role")
    fun changeUserRole(login: String, role: Role): ResponseEntity<String> {
        val user = User.builder()
            .username(login)
            .roles(role.name)
            .build()
        return if(inMemoryUserDetailsManager.userExists(login)) {
            eventProducer.changeUserRole(user)
            ResponseEntity.accepted().build()
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    companion object {
        val encoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}
