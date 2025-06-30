package com.gonzazago.nauta.container.application.consumer

import com.gonzazago.nauta.container.application.usecase.CreateContainer
import com.gonzazago.nauta.container.domain.model.Container
import io.vertx.core.impl.logging.LoggerFactory

class ContainerWorker(private val createContainerUseCase: CreateContainer) {

    private val log = LoggerFactory.getLogger(ContainerWorker::class.java)

    suspend fun processOrderMessage(container: Container) {
        log.info("ContainerWorker: Processing single order from queue: ${container.container}")

        try {
            createContainerUseCase.createContainer(container)
            log.info("ContainerWorker: Container processed successfully: ${container.container}")
        } catch (e: Exception) {
            log.error("ContainerWorker: Failed to process order ${container.container}: ${e.message}", e)

        }
    }
}