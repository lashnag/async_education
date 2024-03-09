package ru.lashnev.accounting

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class EventConsumer(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val accountDao: AccountDao,
    private val eventProducer: EventProducer
) {

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    fun processEvents() {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ConsumerConfig.GROUP_ID_CONFIG] = "task_manager"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        KafkaConsumer<Any?, Any?>(props).use { consumer ->
            consumer.subscribe(listOf("UserStreaming", "TaskStreaming", "TaskWorkflow"))
            while (true) {
                val records = consumer.poll(Duration.ofSeconds(1))
                for (record in records) {
                    try {
                        when (record.key().toString()) {
                            "UserCreated" -> createUser(record.value().toString())
                            "UserRoleChanged" -> updateUserRole(record.value().toString())
                            "TaskCreated" -> createTask(record.value().toString())
                            "TaskAssigned" -> assignTask(record.value().toString())
                            "TaskClosed" -> closeTask(record.value().toString())
                        }
                    } catch (exception: ReplicationBrokenException) {
                        eventProducer.addEvent("BrokenConsumer${record.key().toString()}", record.topic(), record.value().toString())
                    }
                }
            }
        }
    }

    @Transactional
    fun createUser(event: String) {
        try {
            val replicationUser = gson.fromJson(event, ReplicationUser::class.java)
            userDao.save(replicationUser.toUser())
            val account = accountDao.addAccount(replicationUser.publicUserUid)
            eventProducer.accountCreated(account)
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }
    private fun updateUserRole(event: String) {
        try {
            val replicationUser = gson.fromJson(event, ReplicationUser::class.java)
            userDao.updateUserRole(replicationUser.toUser())
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    private fun createTask(event: String) {
        try {
            val task = gson.fromJson(event, ReplicationCreateTask::class.java)
            taskDao.addTask(task.toTask())
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    @Transactional
    fun assignTask(event: String) {
        try {
            val assignedTask = gson.fromJson(event, ReplicationAssignedTask::class.java)
            val task = taskDao.getTask(assignedTask.taskPublicUid)
            if (task.taskStatus == TaskStatus.ASSIGNED) {
                val previousAssignedAccount = accountDao.getUserAccount(task.assignedUserPublicUid!!)
                accountDao.addOperation(
                    account = previousAssignedAccount,
                    amount = -task.assignedPrice!!,
                    description = "Task reassign ${task.description}"
                )
                eventProducer.accountBalanceChanged(accountDao.getUserAccount(task.assignedUserPublicUid!!))
            }
            val randomAssignedPrice = (10..20).shuffled().first().toLong()
            val currentUserAccount = accountDao.getUserAccount(task.assignedUserPublicUid!!)
            accountDao.addOperation(
                account = currentUserAccount,
                amount = randomAssignedPrice,
                description = "Task assigned ${task.description}"
            )
            eventProducer.accountBalanceChanged(accountDao.getUserAccount(task.assignedUserPublicUid!!))
            val randomDonePrice = (20..40).shuffled().first().toLong()
            taskDao.assignTask(
                taskPublicUId = assignedTask.taskPublicUid,
                assignPrice = randomAssignedPrice,
                donePrice = randomDonePrice,
                assignedUserPublicUid = assignedTask.assignedUserPublicUid,
            )
            eventProducer.donePriceCalculated(taskDao.getTask(assignedTask.taskPublicUid))
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    @Transactional
    fun closeTask(event: String) {
        try {
            val closedTask = gson.fromJson(event, ReplicationClosedTask::class.java)
            val task = taskDao.getTask(closedTask.taskPublicUid)
            val currentUserAccount = accountDao.getUserAccount(task.assignedUserPublicUid!!)
            accountDao.addOperation(
                account = currentUserAccount,
                amount = task.donePrice!!,
                description = "Task closed ${task.description}"
            )
            eventProducer.accountBalanceChanged(accountDao.getUserAccount(task.assignedUserPublicUid!!))
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    companion object {
        val gson = Gson()
    }
}
