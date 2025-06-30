package com.gonzazago.nauta.orders.domain.services

import com.gonzazago.nauta.orders.domain.model.Order
import com.gonzazago.nauta.orders.execption.OrderServiceException
import com.gonzazago.nauta.orders.execption.OrderServiceException.Companion.ORDER_ACCESS_DENIED
import com.gonzazago.nauta.orders.execption.OrderServiceException.Companion.PERSISTENCE_ERROR
import com.gonzazago.nauta.orders.repository.order.OrderRepository
import io.vertx.core.impl.logging.LoggerFactory

class OrderService(private val repository: OrderRepository) {

    private val log = LoggerFactory.getLogger(OrderService::class.java)

    suspend fun createOrder(order: Order) {
        try {
            val purchase = repository.getPurchaseByID(order.purchase)
            if (purchase != null && purchase.clientId != order.clientId) {
                log.warn(
                    "OrderService: Access denied for order ${order.purchase}." +
                            " Client ID mismatch: Expected ${order.clientId}," +
                            " Found ${purchase.clientId}."
                )
                throw OrderServiceException(
                    "Order ${order.purchase} belongs to another client.",
                    ORDER_ACCESS_DENIED
                )
            }
            repository.save(order)
        } catch (e: Exception) {
            log.error("Error trying save purchase order", e)
            throw OrderServiceException("Error trying save purchase order", PERSISTENCE_ERROR)
        }
    }

    suspend fun getContainersByPurchaseOrder(orderID: String, clientID: String): List<String> {
        try {
            val purchase = repository.getPurchaseByID(orderID)
            if (purchase != null && purchase.clientId != clientID) {
                log.warn(
                    "OrderService: Access denied for purchaseOrder ${orderID}." +
                            " Client ID mismatch: Expected ${clientID}," +
                            " Found ${purchase.clientId}."
                )
                throw OrderServiceException(
                    "PurchaseOrder ${orderID} belongs to another client.",
                    ORDER_ACCESS_DENIED
                )
            }
            return repository.getContainersByPurchaseOrder(orderID, clientID)
        } catch (e: Exception) {
            log.error("Error try getting container by purchaseID", e)
            throw OrderServiceException(
                "Error try getting container by purchaseID",
                PERSISTENCE_ERROR
            )
        }
    }


    suspend fun getOrderByContainerID(containerID: String, clientID: String): List<Order> {
        return emptyList()

    }

}