package com.gonzazago.nauta.orders.delivery.rest.orders

import com.fasterxml.jackson.databind.ObjectMapper
import com.gonzazago.nauta.orders.application.usecase.orders.CreateOrder
import com.gonzazago.nauta.orders.domain.model.Booking
import com.gonzazago.nauta.orders.mapper.OrderMapper
import com.gonzazago.nauta.orders.utils.parse
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OrderHandler (
    private val objectMapper: ObjectMapper,
    private val mapper: OrderMapper

) {

    // Dirección de la cola interna a la que el Event Bus enviará mensajes
    private val ORDER_QUEUE_ADDRESS = "order.creation.queue"
    private val BOOKING_QUEUE_ADDRESS = "booking.creation.queue" // Asumiendo que tendrás una
    private val CONTAINER_QUEUE_ADDRESS = "container.creation.queue"

    val log = LoggerFactory.getLogger(OrderHandler::class.java)
    fun handleEmailIngestion(ctx: RoutingContext) {
        log.info("Email ingestion request received (simulating /api/email).")

    }
}
