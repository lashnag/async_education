package ru.lashnev.analytics

import org.springframework.stereotype.Repository
import java.util.*

@Repository
class AccountDao {
    private val accounts: MutableSet<Account> = mutableSetOf()

    fun getAccounts(): Set<Account> {
        return accounts
    }

    fun addAccount(account: Account) {
        accounts.add(account)
    }

    fun updateAccountBalance(accountPublicUid: String, currentBalance: Long) {
        val updateAccount = accounts.find { it.accountPublicUid == accountPublicUid }!!
        accounts.remove(updateAccount)
        accounts.add(updateAccount.copy(balance = currentBalance))
    }

    fun addOperation(accountPublicUid: String, operation: Operation) {
        val updateAccount = accounts.find { it.accountPublicUid == accountPublicUid }!!
        updateAccount.operations.add(operation)
    }
}
