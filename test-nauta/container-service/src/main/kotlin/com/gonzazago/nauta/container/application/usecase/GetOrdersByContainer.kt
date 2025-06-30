package com.gonzazago.nauta.container.application.usecase

import com.gonzazago.nauta.container.domain.model.AssociatedOrder
import com.gonzazago.nauta.container.domain.service.ContainerService

class GetOrdersByContainer(private val containerService: ContainerService) {


    suspend fun getOrderByContainer(containerID: String, clientID: String): List<AssociatedOrder>? {

        return containerService.getOrdersByContainer(containerID, clientID)
    }
}