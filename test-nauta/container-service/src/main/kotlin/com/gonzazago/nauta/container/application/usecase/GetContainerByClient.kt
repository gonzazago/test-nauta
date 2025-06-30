package com.gonzazago.nauta.container.application.usecase

import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.domain.service.ContainerService

class GetContainerByClient(private val containerService: ContainerService) {

    suspend fun execute(clientID: String): List<Container> {
        return containerService.getContainerByClient(clientID)
    }
}