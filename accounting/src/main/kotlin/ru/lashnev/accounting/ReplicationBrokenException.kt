package ru.lashnev.accounting

class ReplicationBrokenException: RuntimeException() {
    init {
        println("Send notification to admin")
    }
}
