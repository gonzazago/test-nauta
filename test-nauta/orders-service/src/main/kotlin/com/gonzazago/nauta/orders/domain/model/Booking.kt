package com.gonzazago.nauta.orders.domain.model

data class Booking(
    val booking: String,
    val orders: List<Order>
)