package com.gonzazago.nauta.orchestator.delivery.dto

data class EmailIngestRequest(
    val booking: String?,
    val containers: List<ContainerDTO>? = null,
    val orders: List<OrderDTO>? = null
)