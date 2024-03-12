package ru.lashnev.analytics

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
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
        props[ConsumerConfig.GROUP_ID_CONFIG] = "analytics"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        KafkaConsumer<Any?, Any?>(props).use { consumer ->
            consumer.subscribe(listOf("UserStreaming", "TaskStreaming", "AccountStreaming"))
            while (true) {
                val records = consumer.poll(Duration.ofSeconds(1))
                for (record in records) {
                    try {
                        when (record.key().toString()) {
                            "UserCreated" -> createUser(record.value().toString())
                            "UserRoleChanged" -> updateUserRole(record.value().toString())
                            "TaskCreated" -> createTask(record.value().toString())
                            "DonePriceCalculated" -> donePriceCalculated(record.value().toString())
                            "AccountCreated" -> accountCreated(record.value().toString())
                            "AccountBalanceChanged" -> accountBalanceChanged(record.value().toString())
                            "TaskJiraIdAdded" -> jiraIdAddedToTask(record.value().toString())
                        }
                    } catch (exception: ReplicationBrokenException) {
                        eventProducer.addEvent("AnalyticsBrokenConsumer${record.key().toString()}", record.topic(), record.value().toString())
                    }
                }
            }
        }
    }

    fun createUser(event: String) {
        try {
            val replicationUser = gson.fromJson(event, ReplicationUser::class.java)
            userDao.save(replicationUser.toUser())
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

    private fun donePriceCalculated(event: String) {
        try {
            val donePriceCalculated = gson.fromJson(event, ReplicationTaskDonePriceCalculated::class.java)
            taskDao.updateDonePrice(donePriceCalculated.taskPublicUid, donePriceCalculated.price)
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    private fun accountCreated(event: String) {
        try {
            val replicationAccountCreated = gson.fromJson(event, ReplicationAccountCreated::class.java)
            accountDao.addAccount(replicationAccountCreated.toAccount())
        } catch (exception: RuntimeException) {
            throw ReplicationBrokenException()
        }
    }

    private fun accountBalanceChanged(event: String) {
        try {
            val replicationAccountBalanceChanged = gson.fromJson(event, ReplicationAccountBalanceChanged::class.java)
            accountDao.updateAccountBalance(replicationAccountBalanceChanged.accountPublicUid, replicationAccountBalanceChanged.currentBalance)
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
