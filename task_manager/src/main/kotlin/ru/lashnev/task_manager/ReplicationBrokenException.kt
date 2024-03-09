package ru.lashnev.task_manager

class ReplicationBrokenException: RuntimeException() {
    init {
        println("Send notification to admin")
    }
}
