package com.gonzazago.nauta.orders.domain.model


data class Invoice(
    val id: String,
    val orderPurchaseId: String,
    val clientId: String
)