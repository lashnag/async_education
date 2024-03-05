package ru.lashnev.task_manager

import com.google.gson.Gson
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventProducer {
    fun addTask(task: Task) {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name

        KafkaProducer<Any?, Any?>(props).use { producer ->
            producer.send(
                ProducerRecord(
                    "TaskStreaming",
                    "TaskCreated",
                    gson.toJson(task.toReplicationTask())
                )
            )
        }
    }

    fun taskAssigned(task: Task) {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name

        KafkaProducer<Any?, Any?>(props).use { producer ->
            producer.send(
                ProducerRecord(
                    "TaskWorkflow",
                    "TaskCreated",
                    gson.toJson(task.toReplicationAssignedTask())
                )
            )
        }
    }

    fun closeTask(task: Task) {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name

        KafkaProducer<Any?, Any?>(props).use { producer ->
            producer.send(
                ProducerRecord(
                    "TaskWorkflow",
                    "TaskClosed",
                    gson.toJson(task.toReplicationClosedTask())
                )
            )
        }
    }

    companion object {
        val gson = Gson()
    }
}
