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
        return users.find { it.login == principal }!!
    }

    fun updateUserRole(user: User) {
        val changingUser = users.find { it.login == user.login }!!
        users.remove(changingUser)
        users.add(changingUser.copy(role = user.role))
    }
}
