package com.gonzazago.nauta.container.domain.model

data class Container(
    val container: String,
    val clientId: String,
    val bookingId: String?,
    val associatedOrders: List<AssociatedOrder>? = null,
)

data class AssociatedOrder(
    val orderId: String,
    val clientId: String
)