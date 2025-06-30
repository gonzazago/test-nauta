package com.gonzazago.nauta.orders.delivery.rest.order.dto

data class OrderDTO(
    val purchase: String,
    val invoices: List<InvoiceDTO>
) {
}
