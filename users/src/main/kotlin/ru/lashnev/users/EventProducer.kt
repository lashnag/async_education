package ru.lashnev.users

import com.google.gson.Gson
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventProducer {

    fun addUser(user: UserDetails) {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name

        KafkaProducer<Any?, Any?>(props).use { producer ->
            producer.send(
                ProducerRecord(
                    "CUD",
                    "User.Created",
                    gson.toJson(user.toReplicationUser())
                )
            )
        }
    }

    fun changeUserRole(user: UserDetails) {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name

        KafkaProducer<Any?, Any?>(props).use { producer ->
            producer.send(
                ProducerRecord(
                    "BE",
                    "User.ChangeRole",
                    gson.toJson(user.toReplicationUser())
                )
            )
        }
    }

    companion object {
        val gson = Gson()
    }
}
