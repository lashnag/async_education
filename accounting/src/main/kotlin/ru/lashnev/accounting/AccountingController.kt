package ru.lashnev.accounting

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
class AccountingController(
    private val accountDao: AccountDao,
    private val userDao: UserDao,
    private val bankService: BankService,
    private val messageService: MessageService,
) {
    @GetMapping("/accounting/user_info")
    fun getUserInfo(userPublicUid: String): String {
        val userAccount = accountDao.getUserAccount(userPublicUid)
        return "Balance: ${userAccount.balance}, operations: ${userAccount.operations}"
    }

    @GetMapping("/accounting/private_info")
    fun getPrivateInfo(userPublicUid: String): String {
        val user = userDao.getUser(userPublicUid)
        if(user.role !== Role.ADMIN && user.role !== Role.ACCOUNT) {
            throw IllegalStateException("Forbidden")
        }

        val allAccounts = accountDao.getAccounts()
        val currentSum = allAccounts.sumOf { it.balance }
        return "TotalAmount: $currentSum"
    }

    @GetMapping("/accounting/private_info_by_day")
    fun getPrivateInfoByDay(userPublicUid: String, date: LocalDate): String {
        val user = userDao.getUser(userPublicUid)
        if(user.role !== Role.ADMIN && user.role !== Role.ACCOUNT) {
            throw IllegalStateException("Forbidden")
        }

        val allAccounts = accountDao.getAccounts()
        val operationsByDay = allAccounts.flatMap { it.operations }.filter {
            it.dateTime.toLocalDate() == date
        }
        val amountByDay = operationsByDay.sumOf { it.changeAmount }
        return "Amount by day $date: $amountByDay, operations: $operationsByDay"
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun salaryPayment() {
        val allAccounts = accountDao.getAccounts()
        allAccounts.forEach {
            if(it.balance > 0) {
                val operation = Operation(
                    changeAmount = -it.balance,
                    dateTime = LocalDateTime.now(),
                    description = "Salary payout"
                )

                bankService.payout(it.accountPublicUid, it.balance)
                messageService.notifyPayment(it.userPublicId)
                accountDao.addOperation(it.id, operation)
            }
        }
    }
}
