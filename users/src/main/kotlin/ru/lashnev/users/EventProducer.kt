package ru.lashnev.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.ValidationMessage
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*


@Service
class EventProducer {

    @Value("file:./../../../../../../../replication_schemas/user_streaming/user_created/v1.json")
    private lateinit var userCreatedV1: Resource

    @Value("file:./../../../../../../../replication_schemas/user_streaming/user_role_changed/v1.json")
    private lateinit var userRoleChangedV1: Resource

    fun addUser(user: UserDetails) {
        try {
            addEvent("UserStreaming", "UserCreated", checkSchema(
                gson.toJson(
                    user.toReplicationUser(eventVersion = "V1", producer = "users")
                ), userRoleChangedV1
            )
            )
        } catch (exception: ReplicationContractBrokenException) {
            addEvent(
                "ProducerBrokenUserStreaming", "UserCreated", gson.toJson(
                    user.toReplicationUser(eventVersion = "V1", producer = "users")
                )
            )
        }

    }

    fun changeUserRole(user: UserDetails) {
        try {
            addEvent(
                "UserStreaming", "UserRoleChanged", checkSchema(
                    gson.toJson(
                        user.toReplicationUser(eventVersion = "V1", producer = "users")
                    ), userCreatedV1
                )
            )
        } catch (exception: ReplicationContractBrokenException) {
            addEvent(
                "ProducerBrokenUserStreaming",
                "UserRoleChanged",
                gson.toJson(user.toReplicationUser(eventVersion = "V1", producer = "users"))
            )
        }
    }

    private fun addEvent(topic: String, key: String, json: String) {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        KafkaProducer<Any?, Any?>(props).use { producer -> producer.send(ProducerRecord(topic, key, json)) }
    }

    private fun checkSchema(json: String, schemaResource: Resource): String {
        val schemaFactory = JsonSchemaFactory.getInstance()
        val schema = schemaFactory.getSchema(schemaResource.file.toURI())
        val objectMapper = ObjectMapper()
        val errors: Set<ValidationMessage> = schema.validate(objectMapper.readTree(json))
        if(errors.isNotEmpty()) {
            throw ReplicationContractBrokenException()
        }
        return json
    }

    companion object {
        val gson = Gson()
    }
}
