package com.gonzazago.nauta.orders.application.usecase.orders

import com.gonzazago.nauta.orders.domain.services.OrderService

class GetContainerByPurchaseIDUseCase(
    private val orderService: OrderService
) {

    suspend fun execute(purchaseID: String, clientID: String): List<String> {
        return orderService.getContainersByPurchaseOrder(purchaseID, clientID)
    }
}