package ru.lashnev.users

class ReplicationContractBrokenException: RuntimeException() {
    init {
        println("Send notification to admin")
    }
}
