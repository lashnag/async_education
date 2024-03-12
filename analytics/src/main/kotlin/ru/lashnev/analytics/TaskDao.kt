package ru.lashnev.analytics

import org.springframework.stereotype.Repository

@Repository
class TaskDao {
    private val tasks: MutableSet<Task> = mutableSetOf()

    fun addTask(task: Task) {
        tasks.add(task)
    }

    fun getTasks(): Set<Task> {
        return tasks
    }

    fun updateDonePrice(taskPublicUid: String, donePrice: Long) {
        val updatedTask = tasks.find { it.taskPublicUid == taskPublicUid }!!
        tasks.remove(updatedTask)
        tasks.add(updatedTask.copy(donePrice = donePrice))
    }

    fun updateJiraId(taskPublicUid: String, jiraId: String) {
        val updatedTask = tasks.find { it.taskPublicUid == taskPublicUid }!!
        tasks.remove(updatedTask)
        tasks.add(updatedTask.copy(jiraId = jiraId))
    }
}
