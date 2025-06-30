package com.gonzazago.nauta.orchestator.application.order

import com.gonzazago.nauta.orchestator.infra.publisher.Publisher
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject

class ProcessOrderAction(
    private val orderPublisher: Publisher<JsonObject>,
) {
    private val log = LoggerFactory.getLogger(ProcessOrderAction::class.java)
    private val ORDER_CREATION_QUEUE = "order.creation.queue"

    suspend fun processOrder(orderMessage: JsonObject) {
        val orderId = orderMessage.getString("purchase") ?: orderMessage.getString("id")
        log.info("ProcessOrderAction: Publishing Order message to queue for ID: $orderId")
        orderPublisher.publish(ORDER_CREATION_QUEUE, orderMessage)
    }
}