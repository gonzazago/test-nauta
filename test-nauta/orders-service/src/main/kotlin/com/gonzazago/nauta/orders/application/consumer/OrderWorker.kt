package com.gonzazago.nauta.orders.application.consumer

import com.gonzazago.nauta.orders.application.usecase.orders.CreateOrder
import com.gonzazago.nauta.orders.domain.model.Order
import io.vertx.core.impl.logging.LoggerFactory

class OrderWorker(private val createOrderUseCase: CreateOrder) {

    private val log = LoggerFactory.getLogger(OrderWorker::class.java)

    suspend fun processOrderMessage(order: Order) {
        log.info("OrderWorker: Processing single order from queue: ${order.purchase}")

        try {
            val createdOrder = createOrderUseCase.createOrder(order)
            log.info("OrderWorker: Order processed successfully: ${order.purchase}")
        } catch (e: Exception) {
            log.error("OrderWorker: Failed to process order ${order.purchase}: ${e.message}", e)

        }
    }
}