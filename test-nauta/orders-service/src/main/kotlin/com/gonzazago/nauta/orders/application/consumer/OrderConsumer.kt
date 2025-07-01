package com.gonzazago.nauta.orders.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gonzazago.nauta.orders.domain.model.Order
import com.gonzazago.nauta.orders.mapper.OrderMapper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class OrderQueueConsumer : AbstractVerticle(), KoinComponent {

    private val log = LoggerFactory.getLogger(OrderQueueConsumer::class.java)
    private val orderWorker: OrderWorker by inject()
    private val objectMapper: ObjectMapper by inject()
    private val orderMapper: OrderMapper by inject()
    private val ORDER_QUEUE_ADDRESS = "order.creation.queue"

    override fun start(startPromise: Promise<Void>) {
        log.info("OrderQueueConsumerVerticle starting...")

        vertx.eventBus().consumer<Buffer>(ORDER_QUEUE_ADDRESS) { message ->
            log.info("OrderQueueConsumerVerticle: Received message from Event Bus.")
            processEventBusMessage(message)
        }
            .completionHandler { res ->
                if (res.succeeded()) {
                    log.info("OrderQueueConsumerVerticle is listening on Event Bus address: $ORDER_QUEUE_ADDRESS")
                    startPromise.complete()
                } else {
                    log.error(
                        "OrderQueueConsumerVerticle failed to start consumer: ${res.cause().message}",
                        res.cause()
                    )
                    startPromise.fail(res.cause())
                }
            }
    }

    private fun processEventBusMessage(message: Message<Buffer>) {
        val messageBodyWrapper: Buffer = message.body()
        val messageBodyJson= messageBodyWrapper.toJsonObject()
        val actualOrderJson: JsonObject = messageBodyJson.getJsonObject("map")
        GlobalScope.launch(vertx.dispatcher()) {
            try {
                val actualOrderJsonString = actualOrderJson.encode()
                val order = objectMapper.readValue<Order>(actualOrderJsonString)
                orderWorker.processOrderMessage(order)
                message.reply("ACK")
            } catch (e: Exception) {
                log.error("OrderQueueConsumerVerticle: Error processing Event Bus message: ${e.message}", e)
                message.fail(500, e.message)
            }
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        log.info("OrderQueueConsumerVerticle stopping...")
        stopPromise.complete()
    }
}