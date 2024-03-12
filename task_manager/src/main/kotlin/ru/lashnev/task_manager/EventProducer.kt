package ru.lashnev.task_manager

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

    @Value("file:./../../../../../../../replication_schemas/task_streaming/task_created/v2.json")
    private lateinit var taskCreatedV1: Resource

    @Value("file:./../../../../../../../replication_schemas/user_streaming/user_role_changed/v1.json")
    private lateinit var taskAssignedV1: Resource

    @Value("file:./../../../../../../../replication_schemas/user_streaming/user_role_changed/v1.json")
    private lateinit var taskClosedV1: Resource

    @Value("file:./../../../../../../../replication_schemas/task_streaming/task_jira_id_added/v1.json")
    private lateinit var taskJiraIdAddedV1: Resource

    fun addTask(task: Task) {
        try {
            addEvent(
                "TaskStreaming", "TaskCreated", checkSchema(
                    gson.toJson(
                        task.toReplicationTask(
                            eventVersion = "V1",
                            producer = "task_manager"
                        )
                    ), taskCreatedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "TaskManagerProducerBrokenTaskStreaming", "TaskCreated", gson.toJson(
                    task.toReplicationTask(eventVersion = "V1", producer = "task_manager")
                )
            )
        }
    }

    fun taskAssigned(task: Task) {
        try {
            addEvent(
                "TaskWorkflow", "TaskAssigned", checkSchema(
                    gson.toJson(task.toReplicationAssignedTask(eventVersion = "V1", producer = "task_manager")),
                    taskAssignedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "TaskManagerProducerBrokenTaskWorkflow", "TaskAssigned", gson.toJson(
                    task.toReplicationAssignedTask(eventVersion = "V1", producer = "task_manager")
                )
            )
        }
    }

    fun closeTask(task: Task) {
        try {
            addEvent(
                "TaskWorkflow", "TaskClosed", checkSchema(
                    gson.toJson(
                        task.toReplicationClosedTask(eventVersion = "V1", producer = "task_manager")
                    ), taskClosedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "TaskManagerProducerBrokenTaskWorkflow", "TaskClosed", gson.toJson(
                    task.toReplicationClosedTask(eventVersion = "V1", producer = "task_manager")
                )
            )
        }
    }

    fun addJiraIdToTask(task: Task) {
        try {
            addEvent(
                "TaskStreaming", "TaskJiraIdAdded", checkSchema(
                    gson.toJson(
                        task.toReplicationJiraIdAdded(
                            eventVersion = "V1",
                            producer = "task_manager"
                        )
                    ), taskJiraIdAddedV1
                )
            )
        } catch (exception: ReplicationBrokenException) {
            addEvent(
                "TaskManagerProducerBrokenTaskStreaming", "TaskJiraIdAdded", gson.toJson(
                    task.toReplicationJiraIdAdded(eventVersion = "V1", producer = "task_manager")
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
