package ru.lashnev.accounting

import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class AccountDao {
    private val accounts: MutableSet<Account> = mutableSetOf()

    fun addAccount(userPublicUid: String): Account {
        val account = Account(
            id = UUID.randomUUID().toString(),
            balance = 0,
            accountPublicUid = UUID.randomUUID(),
            userPublicId = userPublicUid,
            operations = mutableListOf(),
        )
        accounts.add(account)
        return account
    }

    fun getUserAccount(userPublicUid: String): Account {
        return accounts.find { it.userPublicId == userPublicUid}!!
    }

    fun getAccounts(): Set<Account> {
        return accounts
    }

    fun addOperation(account: Account, amount: Long, description: String) {
        val changedAccount = accounts.find { it.id == account.id }!!
        accounts.remove(changedAccount)
        changedAccount.operations.add(
            Operation(
                changeAmount = amount,
                dateTime = LocalDateTime.now(),
                description = description
            )
        )
        accounts.add(changedAccount.copy(balance = changedAccount.balance + amount))
    }

}
