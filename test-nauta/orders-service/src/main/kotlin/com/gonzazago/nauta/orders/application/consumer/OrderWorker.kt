package com.gonzazago.nauta.orders.application.consumer

import com.gonzazago.nauta.orders.application.usecase.orders.CreateOrder
import com.gonzazago.nauta.orders.domain.model.Order
import io.vertx.core.impl.logging.LoggerFactory

class OrderWorker(private val createOrderUseCase: CreateOrder) {

    private val log = LoggerFactory.getLogger(OrderWorker::class.java)

    suspend fun processOrderMessage(order: Order) {

        try {
            val createdOrder = createOrderUseCase.createOrder(order)
        } catch (e: Exception) {
            log.error("OrderWorker: Failed to process order ${order.purchase}: ${e.message}", e)

        }
    }
}