package ru.lashnev.accounting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class AccountingApplication

fun main(args: Array<String>) {
    runApplication<AccountingApplication>(*args)
}
