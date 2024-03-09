package ru.lashnev.analytics

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalStateException
import java.time.Duration
import java.time.LocalDateTime

@RestController
class AnalyticsController(
    private val userDao: UserDao,
    private val accountDao: AccountDao,
    private val taskDao: TaskDao,
) {
    @GetMapping("/analytics/today_income")
    fun todayIncome(userPublicUid: String): String {
        val user = userDao.getUser(userPublicUid)
        if(user.role != Role.ADMIN) {
            throw IllegalStateException("Forbidden")
        }

        val currentBalance = accountDao.getAccounts().sumOf { it.balance }
        return "Current balance: $currentBalance"
    }

    @GetMapping("/analytics/minus_users")
    fun getAllMinusUsers(userPublicUid: String): String {
        val user = userDao.getUser(userPublicUid)
        if(user.role != Role.ADMIN) {
            throw IllegalStateException("Forbidden")
        }

        val allMinusAccounts = accountDao.getAccounts().filter { it.balance < 0 }
        return allMinusAccounts.toString()
    }

    @GetMapping("/analytics/the_most_expensive_task")
    fun getTheMostExpensiveTask(userPublicUid: String, duration: Duration): String {
        val user = userDao.getUser(userPublicUid)
        if(user.role != Role.ADMIN) {
            throw IllegalStateException("Forbidden")
        }

        val suitableTasks = taskDao.getTasks().filter {
            Duration.between(it.creationDate, LocalDateTime.now()) <= duration
        }
        val theMostExpensiveTask = suitableTasks.maxBy { it.donePrice!! }
        return "The most expensive task is $theMostExpensiveTask"
    }
}
