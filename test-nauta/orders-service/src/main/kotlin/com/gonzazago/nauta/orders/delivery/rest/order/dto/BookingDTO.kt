package com.gonzazago.nauta.orders.delivery.rest.order.dto

data class BookingDTO(
    val booking: String,
    val orders: List<OrderDTO>
)