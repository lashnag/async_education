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
        props[ConsumerConfig.GROUP_ID_CONFIG] = "accounting"
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
                            "TaskJiraIdAdded" -> jiraIdAddedToTask(record.value().toString())
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
            when(event.getReplicationVersion()) {
                ReplicationVersion.V1 -> {
                    val task = gson.fromJson(event, ReplicationCreateTaskV1::class.java)
                    taskDao.addTask(task.toTask())
                }
                ReplicationVersion.V2 -> {
                    val task = gson.fromJson(event, ReplicationCreateTaskV2::class.java)
                    taskDao.addTask(task.toTask())
                }
            }
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
                    description = "Task reassign ${task.title}"
                )
                eventProducer.accountBalanceChanged(accountDao.getUserAccount(task.assignedUserPublicUid!!))
            }
            val randomAssignedPrice = (10..20).shuffled().first().toLong()
            val currentUserAccount = accountDao.getUserAccount(task.assignedUserPublicUid!!)
            accountDao.addOperation(
                account = currentUserAccount,
                amount = randomAssignedPrice,
                description = "Task assigned ${task.title}"
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
                description = "Task closed ${task.title}"
            )
            eventProducer.accountBalanceChanged(accountDao.getUserAccount(task.assignedUserPublicUid!!))
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }


    private fun jiraIdAddedToTask(event: String) {
        try {
            val jiraIdAdded = gson.fromJson(event, ReplicationJiraIdAddedToTask::class.java)
            taskDao.updateJiraId(jiraIdAdded.taskPublicUid, jiraIdAdded.jiraId)
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    private fun String.getReplicationVersion(): ReplicationVersion {
        if(this.contains("V1")) {
            return ReplicationVersion.V1
        }
        if(this.contains("V2")) {
            return ReplicationVersion.V2
        }

        throw java.lang.RuntimeException()
    }

    enum class ReplicationVersion {
        V1, V2
    }

    companion object {
        val gson = Gson()
    }
}
