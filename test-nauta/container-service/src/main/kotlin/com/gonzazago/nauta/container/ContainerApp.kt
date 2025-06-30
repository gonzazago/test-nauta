package com.gonzazago.nauta.container

import com.gonzazago.nauta.container.application.consumer.ContainerQueueConsumer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import org.koin.core.component.KoinComponent


class ContainerMain : AbstractVerticle(), KoinComponent {

    private val log = LoggerFactory.getLogger(ContainerMain::class.java)

    override fun start(startPromise: Promise<Void>) {
        log.info("Container service logical Verticle (ContainerMain.kt) starting...")

        vertx.deployVerticle(ContainerQueueConsumer::class.java.name) { res ->
            if (res.succeeded()) {
                log.info("ContainerQueueConsumerVerticle deployed successfully for BookingService.")
                startPromise.complete()
            } else {
                log.error("ContainerQueueConsumerVerticle failed to deploy for BookingService!", res.cause())
                startPromise.fail(res.cause())
            }
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        log.info("Container service logical Verticle (ContainerMain.kt) stopping...")
        stopPromise.complete()
    }
}