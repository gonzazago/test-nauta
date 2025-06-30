package com.gonzazago.nauta.orders

import com.gonzazago.nauta.orders.application.consumer.OrderQueueConsumer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import org.koin.core.component.KoinComponent


class OrderMain : AbstractVerticle(), KoinComponent {

    private val log = LoggerFactory.getLogger(OrderMain::class.java)

    override fun start(startPromise: Promise<Void>) {
        log.info("Order service logical Verticle (OrderMain.kt) starting...")
        vertx.deployVerticle(OrderQueueConsumer::class.java.name) { res ->
            if (res.succeeded()) {
                log.info("OrderQueueConsumerVerticle deployed successfully for OrderService.")
                startPromise.complete()
            } else {
                log.error("OrderQueueConsumerVerticle failed to deploy for OrderService!", res.cause())
                startPromise.fail(res.cause())
            }
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        log.info("Order service logical Verticle (OrderMain.kt) stopping...")
        stopPromise.complete()
    }
}






