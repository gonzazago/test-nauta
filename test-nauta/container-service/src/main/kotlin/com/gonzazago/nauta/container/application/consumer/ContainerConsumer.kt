package com.gonzazago.nauta.container.application.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gonzazago.nauta.container.domain.model.Container
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ContainerQueueConsumer : AbstractVerticle(), KoinComponent {

    private val log = LoggerFactory.getLogger(ContainerQueueConsumer::class.java)
    private val containerWorker: ContainerWorker by inject()
    private val objectMapper: ObjectMapper by inject()
    private val CONTAINER_QUEUE_ADDRESS = "container.creation.queue"

    override fun start(startPromise: Promise<Void>) {
        log.info("ContainerQueueConsumerVerticle starting...")

        vertx.eventBus().consumer(CONTAINER_QUEUE_ADDRESS) { message ->
            processEventBusMessage(message)
        }
            .completionHandler { res ->
                if (res.succeeded()) {
                    log.info("ContainerQueueConsumerVerticle is listening on Event Bus address: $CONTAINER_QUEUE_ADDRESS")
                    startPromise.complete()
                } else {
                    log.error(
                        "ContainerQueueConsumerVerticle failed to start consumer: ${res.cause().message}",
                        res.cause()
                    )
                    startPromise.fail(res.cause())
                }
            }
    }

    private fun processEventBusMessage(message: Message<Buffer>) {
        val messageBodyWrapper: Buffer = message.body()
        val messageBodyJson = messageBodyWrapper.toJsonObject()


        GlobalScope.launch(vertx.dispatcher()) {
            try {
                val actualContainerJsonString = messageBodyJson.encode()
                val container = objectMapper.readValue<Container>(actualContainerJsonString)
                containerWorker.processOrderMessage(container)

                message.reply("ACK")
            } catch (e: Exception) {
                log.error("ContainerQueueConsumerVerticle: Error processing Event Bus message: ${e.message}", e)
                message.fail(500, e.message)
            }
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        log.info("ContainerQueueConsumerVerticle stopping...")
        stopPromise.complete()
    }
}