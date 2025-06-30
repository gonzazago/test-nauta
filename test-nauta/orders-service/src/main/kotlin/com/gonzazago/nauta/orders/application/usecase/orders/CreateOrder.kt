package com.gonzazago.nauta.orders.application.usecase.orders

import com.gonzazago.nauta.orders.domain.model.Order
import com.gonzazago.nauta.orders.domain.services.OrderService

class CreateOrder (private val orderService: OrderService) {


    suspend fun createOrder(order: Order){
    orderService.createOrder(order)
    }


}