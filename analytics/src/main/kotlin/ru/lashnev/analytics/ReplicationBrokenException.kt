package ru.lashnev.analytics

class ReplicationBrokenException: RuntimeException() {
    init {
        println("Send notification to admin")
    }
}
