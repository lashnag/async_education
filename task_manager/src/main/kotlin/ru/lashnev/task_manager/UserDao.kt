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

    fun getUser(userPublicId: String): User {
        return users.find { it.publicUid == userPublicId }!!
    }

    fun updateUserRole(user: User) {
        val changingUser = users.find { it.publicUid == user.publicUid }!!
        users.remove(changingUser)
        users.add(changingUser.copy(role = user.role))
    }
}
