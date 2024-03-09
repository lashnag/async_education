package ru.lashnev.task_manager

import com.google.gson.Gson
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class EventConsumer(private val userDao: UserDao, private val eventProducer: EventProducer) {

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    fun processEvents() {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ConsumerConfig.GROUP_ID_CONFIG] = "task_manager"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        KafkaConsumer<Any?, Any?>(props).use { consumer ->
            consumer.subscribe(listOf("UserStreaming"))
            while (true) {
                val records = consumer.poll(Duration.ofSeconds(1))
                for (record in records) {
                    try {
                        when (record.key().toString()) {
                            "UserCreated" -> createUser(record.value().toString())
                            "UserRoleChanged" -> updateUserRole(record.value().toString())
                        }
                    } catch (exception: ReplicationBrokenException) {
                        eventProducer.addEvent("BrokenConsumer${record.key().toString()}", record.topic(), record.value().toString())
                    }
                }
            }
        }
    }

    private fun createUser(event: String) {
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

    companion object {
        val gson = Gson()
    }
}
