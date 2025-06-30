package com.gonzazago.nauta.orders.repository.order

import com.gonzazago.nauta.orders.domain.model.Order

interface OrderRepository {
    suspend fun save(order: Order): Order
    suspend fun getPurchaseByID(purchaseID: String): Order?
    suspend fun getPurchaseOrdersByClient(clientID:String):List<Order>
    suspend fun getContainersByPurchaseOrder(orderID:String, clientID: String):List<String>

}