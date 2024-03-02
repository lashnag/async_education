package ru.lashnev.task_manager

import org.springframework.stereotype.Repository

@Repository
class UserDao {

    private val users: MutableSet<User> = mutableSetOf(
        User("someUser1", Role.OTHER),
        User("someUser2", Role.OTHER)
    )

    fun save(user: User) {
        users.add(user)
    }

    fun getAllUsers(): Set<User> {
        return users
    }

    fun getUser(principal: String): User {
        return users.find { it.principal == principal }!!
    }
}
