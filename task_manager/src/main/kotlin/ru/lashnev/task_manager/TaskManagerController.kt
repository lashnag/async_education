package ru.lashnev.task_manager

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TaskManagerController(
    private val taskDao: TaskDao,
    private val userDao: UserDao,
    private val eventProducer: EventProducer
) {

    @GetMapping("/task_manager/list")
    fun getTasks(principal: String): Set<Task> {
        return taskDao.getUserTasks(principal)
    }

    @PutMapping("task_manager/create_task")
    fun createNewTask(principal: String, taskDescription: String): String {
        val assignedUser = userDao
            .getAllUsers()
            .filter { it.role != Role.MANAGER && it.role != Role.ADMIN }
            .random()
        val task = Task(UUID.randomUUID(), principal, taskDescription, assignedUser.login)
        taskDao.save(task)
        eventProducer.addTask(task)
        return "Created"
    }

    @PostMapping("task_manager/close_task")
    fun closeTask(principal: String, taskUUID: UUID): String {
        val task = taskDao.getTask(principal, taskUUID)
        return if(task != null) {
            taskDao.closeTask(taskUUID)
            eventProducer.closeTask(task)
            "Closed"
        } else {
            "Cant close"
        }
    }

    @PostMapping("task_manager/reassign")
    fun reassignOpenTasks(principal: String): String {
        val user = userDao.getUser(principal)
        if(user.role != Role.MANAGER && user.role != Role.ADMIN) {
            return "Permission denied"
        }
        val usersToAssign = userDao.getAllUsers().filter { it.role != Role.MANAGER && it.role != Role.ADMIN }
        val openTasks = taskDao.getOpenTasks()
        openTasks.forEach {
            taskDao.reassign(it, usersToAssign.random().login)
        }
        return "Reassigned"
    }
}
