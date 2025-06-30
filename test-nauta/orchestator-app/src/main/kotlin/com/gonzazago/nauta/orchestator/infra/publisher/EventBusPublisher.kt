package com.gonzazago.nauta.orchestator.infra.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject

class EventBusPublisher<T>(
    private val vertx: Vertx,
    private val objectMapper: ObjectMapper
) : Publisher<T> {

    private val log = LoggerFactory.getLogger(EventBusPublisher::class.java)
    override suspend fun publish(topic: String, message: T) {
        val messageJsonString = objectMapper.writeValueAsString(message)
        val messageJsonObject = JsonObject(messageJsonString)
        vertx.eventBus().publish(topic, Buffer.buffer(messageJsonObject.encode()))
        log.info("EventBus: Published message to address '$topic'. Message type: ${message!!::class.java.name}")


    }

}