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
                            "AccountCreated" -> createAccount(record.value().toString())
                            "AccountBalanceChanged" -> accountBalanceChanged(record.value().toString())
                        }
                    } catch (exception: ReplicationBrokenException) {
                        eventProducer.addEvent("BrokenConsumer${record.key().toString()}", record.topic(), record.value().toString())
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
            val task = gson.fromJson(event, ReplicationCreateTask::class.java)
            taskDao.addTask(task.toTask())
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

    private fun createAccount(event: String) {
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

    companion object {
        val gson = Gson()
    }
}
