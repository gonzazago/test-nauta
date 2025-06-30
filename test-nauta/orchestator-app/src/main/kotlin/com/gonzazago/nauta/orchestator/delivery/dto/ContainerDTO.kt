package com.gonzazago.nauta.orchestator.delivery.dto

class ContainerDTO(
    val container: String,
)


data class AssociatedOrderDTO(
    val orderId: String,
    val clientId: String
)

data class ContainerMessageDTO(
    val container: String,
    val clientId: String,
    val bookingId: String?,
    val associatedOrders: List<AssociatedOrderDTO>? = null,
)
