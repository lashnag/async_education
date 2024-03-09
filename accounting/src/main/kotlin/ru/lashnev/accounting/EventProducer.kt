package ru.lashnev.accounting

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
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventProducer {

    @Value("file:./../../../../../../../replication_schemas/account_streaming/account_created/v1.json")
    private lateinit var accountCreatedV1: Resource

    @Value("file:./../../../../../../../replication_schemas/account_streaming/account_balance_changed/v1.json")
    private lateinit var accountChangedV1: Resource

    @Value("file:./../../../../../../../replication_schemas/account_streaming/done_price_calculated/v1.json")
    private lateinit var donePriceCalculatedV1: Resource

    fun accountCreated(account: Account) {
        try {
            addEvent(
                "AccountStreaming", "AccountCreated", checkSchema(
                    gson.toJson(
                        account.toReplicationAccountCreated(
                            eventVersion = "V1",
                            producer = "task_manager"
                        )
                    ), accountCreatedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "ProducerBrokenAccountStreaming", "AccountCreated", gson.toJson(
                    account.toReplicationAccountCreated(eventVersion = "V1", producer = "task_manager")
                )
            )
        }
    }

    fun accountBalanceChanged(account: Account) {
        try {
            addEvent(
                "AccountStreaming", "AccountBalanceChanged", checkSchema(
                    gson.toJson(account.toReplicationAccountBalanceChanged(eventVersion = "V1", producer = "task_manager")),
                    accountChangedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "ProducerBrokenAccountStreaming", "AccountBalanceChanged", gson.toJson(
                    account.toReplicationAccountBalanceChanged(eventVersion = "V1", producer = "task_manager")
                )
            )
        }
    }

    fun donePriceCalculated(task: Task) {
        try {
            addEvent(
                "AccountStreaming", "DonePriceCalculated", checkSchema(
                    gson.toJson(
                        task.toReplicationTaskDonePriceCalculated(eventVersion = "V1", producer = "task_manager")
                    ), donePriceCalculatedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "ProducerBrokenAccountStreaming", "DonePriceCalculated", gson.toJson(
                    task.toReplicationTaskDonePriceCalculated(eventVersion = "V1", producer = "task_manager")
                )
            )
        }
    }

    fun addEvent(topic: String, key: String, json: String) {
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
            throw ReplicationBrokenException()
        }
        return json
    }

    companion object {
        val gson = Gson()
    }
}