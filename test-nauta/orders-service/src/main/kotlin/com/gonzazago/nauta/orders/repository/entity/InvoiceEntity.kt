package com.gonzazago.nauta.orders.repository.entity

data class InvoiceEntity(
    val id: String,
    val clientId: String,
    val orderPurchaseId: String
)