package com.gonzazago.nauta.orders.mapper

import com.gonzazago.nauta.orders.domain.model.Order
import com.gonzazago.nauta.orders.domain.model.Invoice
import com.gonzazago.nauta.orders.repository.entity.OrderEntity
import com.gonzazago.nauta.orders.repository.entity.InvoiceEntity
import com.gonzazago.nauta.orders.delivery.rest.order.dto.OrderDTO // <-- Este es el DTO de entrada correcto
import com.gonzazago.nauta.orders.delivery.rest.order.dto.InvoiceDTO // <-- Este es el DTO de entrada correcto



class OrderMapper {

    fun toDomainModel(dto: InvoiceDTO, orderPurchaseId: String, clientId: String): Invoice {
        return Invoice(
            id = dto.invoice,
            orderPurchaseId = orderPurchaseId,
            clientId = clientId
        )
    }

    fun toEntity(model: Order): OrderEntity {
        return OrderEntity(
            id = model.purchase,
            clientId = model.clientId,
            bookingId = model.bookingId
        )
    }

    fun toEntity(model: Invoice): InvoiceEntity {
        return InvoiceEntity(
            id = model.id,
            orderPurchaseId = model.orderPurchaseId,
            clientId = model.clientId
        )
    }

    fun toDomainModel(entity: OrderEntity, relatedInvoices: List<Invoice>? = null): Order {
        return Order(
            purchase = entity.id,
            clientId = entity.clientId,
            bookingId = entity.bookingId,
            invoices = relatedInvoices ?: emptyList() // Asigna las invoices cargadas
        )
    }

    // --- Mapper de Modelo de Dominio (Model) a DTO de Salida (ResponseDto) ---
//    // Usado por la capa de entrega (OrderHandler/GetOrdersHandler)
//    fun toResponseDto(model: Order): OrderResponseDto {
//        val responseInvoices = model.invoices?.map { toResponseDto(it) } ?: emptyList()
//        return OrderResponseDto(
//            id = model.id,
//            clientId = model.clientId,
//            bookingId = model.bookingId,
//            purchase = model.id, // 'purchase' en la salida es el 'id' del dominio
//            invoices = responseInvoices
//        )
//    }
//
//    fun toResponseDto(model: Invoice): InvoiceResponseDto {
//        return InvoiceResponseDto(
//            id = model.id,
//            invoice = model.id // 'invoice' en la salida es el 'id' del dominio
//        )
//    }
}