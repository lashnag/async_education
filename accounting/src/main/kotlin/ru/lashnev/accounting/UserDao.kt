package ru.lashnev.accounting

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

    fun getUser(userPublicId: String): User {
        return users.find { it.userPublicUid == userPublicId }!!
    }

    fun updateUserRole(user: User) {
        val changingUser = users.find { it.userPublicUid == user.userPublicUid }!!
        users.remove(changingUser)
        users.add(changingUser.copy(role = user.role))
    }
}
