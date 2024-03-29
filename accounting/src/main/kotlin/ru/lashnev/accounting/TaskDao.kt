package ru.lashnev.accounting

import org.springframework.stereotype.Repository

@Repository
class TaskDao {
    private val tasks: MutableSet<Task> = mutableSetOf()

    fun addTask(task: Task) {
        tasks.add(task)
    }

    fun assignTask(taskPublicUId: String, assignedUserPublicUid: String, assignPrice: Long, donePrice: Long) {
        val updatedTask = tasks.find { it.taskPublicUid == taskPublicUId }!!
        tasks.remove(updatedTask)
        tasks.add(
            updatedTask.copy(
                assignedUserPublicUid = assignedUserPublicUid,
                assignedPrice = assignPrice,
                donePrice = donePrice,
                taskStatus = TaskStatus.ASSIGNED,
            )
        )
    }

    fun getTask(taskPublicUid: String): Task {
        return tasks.find { it.taskPublicUid == taskPublicUid }!!
    }

    fun updateJiraId(taskPublicUid: String, jiraId: String) {
        val updatedTask = tasks.find { it.taskPublicUid == taskPublicUid }!!
        tasks.remove(updatedTask)
        tasks.add(updatedTask.copy(jiraId = jiraId))
    }
}
