package com.gonzazago.nauta.orchestator.delivery.dto

data class BookingDTO(
    val booking: String,
    val orders: List<OrderDTO>
)