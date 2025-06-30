package com.gonzazago.nauta.container.application.usecase

import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.domain.service.ContainerService
import io.vertx.core.impl.logging.LoggerFactory

class CreateContainer(private val containerService: ContainerService) {

    suspend fun createContainer(container: Container) {
        containerService.createContainer(container)
    }

}