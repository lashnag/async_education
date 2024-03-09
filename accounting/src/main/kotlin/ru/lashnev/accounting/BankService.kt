package ru.lashnev.accounting

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BankService {
    fun payout(accountPublicId: UUID, amount: Long) {}
}
