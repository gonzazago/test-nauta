package com.gonzazago.nauta.container.delivery.dto

data class ContainerRequestDto(
    val id: String,
    val clientId: String,
    val bookingId: String?,
    val associatedOrderIds: List<String>? = null,
)

data class ContainerResponseDto(
    val id: String,
    val clientId: String,
    val bookingId: String?,
    val associatedOrders: List<AssociatedOrderDTO>? = null
)

data class AssociatedOrderDTO(
    val orderId: String,
    val clientId: String
)
