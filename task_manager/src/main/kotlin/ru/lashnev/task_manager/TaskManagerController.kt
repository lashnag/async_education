package ru.lashnev.task_manager

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalStateException
import java.util.UUID
import kotlin.random.Random

@RestController
class TaskManagerController(
    private val taskDao: TaskDao,
    private val userDao: UserDao,
    private val eventProducer: EventProducer
) {

    @GetMapping("/task_manager/list")
    fun getTasks(userPublicUid: String): Set<Task> {
        return taskDao.getUserTasks(userPublicUid)
    }

    @PutMapping("task_manager/create_task")
    fun createNewTask(userPublicUid: String, taskDescription: String, taskTitle: String, jiraId: String): String {
        if(taskTitle.contains("[") || taskTitle.contains("]")) {
            throw IllegalStateException("No jira ticket in taskTitle")
        }

        val assignedUser = userDao
            .getAllUsers()
            .filter { it.role != Role.MANAGER && it.role != Role.ADMIN }
            .random()
        val task = Task(
            id = Random.nextInt(),
            taskPublicUid = UUID.randomUUID(),
            authorPublicUid = userPublicUid,
            description = taskDescription,
            assignedUserPublicUid = assignedUser.publicUid,
            title = taskTitle,
            jiraId = jiraId,
        )

        taskDao.save(task)
        eventProducer.addTask(task)
        eventProducer.taskAssigned(task)
        return "Created"
    }

    @PostMapping("task_manager/close_task")
    fun closeTask(userPublicUid: String, taskPublicUid: UUID): String {
        val task = taskDao.getTask(userPublicUid, taskPublicUid)
        return if(task != null) {
            taskDao.closeTask(taskPublicUid)
            eventProducer.closeTask(task)
            "Closed"
        } else {
            "Cant close"
        }
    }

    @PostMapping("task_manager/reassign")
    fun reassignOpenTasks(userPublicUid: String): String {
        val user = userDao.getUser(userPublicUid)
        if(user.role != Role.MANAGER && user.role != Role.ADMIN) {
            return "Permission denied"
        }
        val usersToAssign = userDao.getAllUsers().filter { it.role != Role.MANAGER && it.role != Role.ADMIN }
        val openTasks = taskDao.getOpenTasks()
        openTasks.forEach {
            taskDao.reassign(it, usersToAssign.random().publicUid)
        }
        return "Reassigned"
    }
}
