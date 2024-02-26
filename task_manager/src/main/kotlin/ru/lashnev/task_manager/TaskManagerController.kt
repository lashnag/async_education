package ru.lashnev.task_manager

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskManagerController() {
    @GetMapping("/task_manager/list")
    fun getArticles(): Array<String> {
        return arrayOf("Task 1", "Task 2")
    }
}
