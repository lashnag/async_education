package ru.lashnev.analytics

import org.springframework.stereotype.Repository

@Repository
class TaskDao {
    private val tasksWithoutPrice: MutableSet<PartialTaskWithoutPrice> = mutableSetOf()
    private val taskPrices: MutableSet<TaskPrice> = mutableSetOf()

    fun addTaskWithoutPrice(task: PartialTaskWithoutPrice) {
        tasksWithoutPrice.add(task)
    }

    fun getTasks(): Set<Task> {
        return tasksWithoutPrice.filter { partialTask ->
            taskPrices.any { it.taskPublicUid ==  partialTask.taskPublicUid }
        }.map { partialTask ->
        val donePrice = taskPrices.find { it.taskPublicUid == partialTask.taskPublicUid }!!
            Task(
                taskPublicUid = partialTask.taskPublicUid,
                title = partialTask.title,
                creationTime = partialTask.creationTime,
                jiraId = partialTask.jiraId,
                donePrice = donePrice.donePrice
            )
        }.toSet()
    }

    fun addDonePrice(taskPublicUid: String, donePrice: Long) {
        taskPrices.add(TaskPrice(taskPublicUid = taskPublicUid, donePrice = donePrice))
    }

    fun updateJiraId(taskPublicUid: String, jiraId: String) {
        val updatedTask = tasksWithoutPrice.find { it.taskPublicUid == taskPublicUid }!!
        tasksWithoutPrice.remove(updatedTask)
        tasksWithoutPrice.add(updatedTask.copy(jiraId = jiraId))
    }
}
