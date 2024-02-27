package ru.lashnev.users

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class EventProducer {
    fun addUser(user: UserDetails) {
        // TODO("Message broker")
    }

    fun changeUserRole(user: UserDetails) {
        // TODO("Message broker")
    }
}
