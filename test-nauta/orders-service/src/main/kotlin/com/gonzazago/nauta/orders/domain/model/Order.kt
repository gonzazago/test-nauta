package com.gonzazago.nauta.orders.domain.model

data class Order(
    val purchase: String,
    val clientId: String,
    val bookingId: String?,
    val invoices: List<Invoice>,
    val containerIds: List<String>? = null
)
