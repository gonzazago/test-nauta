package com.gonzazago.nauta.orchestator.application.container

import com.gonzazago.nauta.orchestator.delivery.dto.ContainerMessageDTO
import com.gonzazago.nauta.orchestator.infra.publisher.Publisher
import io.vertx.core.impl.logging.LoggerFactory

class ProcessContainerAction(
    private val containerPublisher: Publisher<ContainerMessageDTO>,
) {
    private val log = LoggerFactory.getLogger(ProcessContainerAction::class.java)
    private val CONTAINER_CREATION_QUEUE = "container.creation.queue"

    suspend fun processContainer(message: ContainerMessageDTO) {
        log.info("ProcessContainerAction: Publishing container message to queue for ID: ${message.container}")
        containerPublisher.publish(CONTAINER_CREATION_QUEUE, message)
    }
}