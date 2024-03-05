package ru.lashnev.task_manager

import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.random.Random

@Repository
class TaskDao {
    private val tasks: MutableSet<Task> = mutableSetOf(
        Task(Random.nextInt(), UUID.randomUUID(), "someUser1", "TestTask", "someUser2", TaskStatus.OPEN),
        Task(Random.nextInt(), UUID.randomUUID(), "someUser1", "TestTask2", "admin", TaskStatus.OPEN),
    )

    fun save(task: Task) {
        tasks.add(task)
    }

    fun getUserTasks(user: String): Set<Task> {
        return tasks.filter { it.assignedUserPublicUid == user }.toSet()
    }

    fun getTask(user: String, taskUUID: UUID): Task? {
        return tasks.find { it.assignedUserPublicUid == user && it.publicUid == taskUUID }
    }

    fun closeTask(taskUUID: UUID) {
        val taskToClose = tasks.find { it.publicUid == taskUUID }
        tasks.remove(taskToClose)
        taskToClose?.copy(status = TaskStatus.CLOSED)?.let { tasks.add(it) }
    }

    fun getOpenTasks(): Set<Task> {
        return tasks.filter { it.status == TaskStatus.OPEN }.toSet()
    }

    fun reassign(task: Task, principal: String) {
        val taskToReassign = tasks.find { it.publicUid == task.publicUid }!!
        tasks.remove(taskToReassign)
        tasks.add(task.copy(assignedUserPublicUid = principal))
    }
}
