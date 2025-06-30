package com.gonzazago.nauta.orchestator.delivery.dto

data class OrderDTO(
    val purchase: String,
    val invoices: List<InvoiceDTO>
) {
}
