package ru.lashnev.task_manager

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class EventConsumer {

    @Scheduled(fixedDelay = 10_000)
    fun processEvents() {
        // TODO("Not implemented")
    }
}
