package com.gonzazago.nauta.container.domain.service

import com.gonzazago.nauta.container.domain.model.AssociatedOrder
import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.execption.ContainerServiceException
import com.gonzazago.nauta.container.execption.ContainerServiceException.Companion.PERSISTENCE_ERROR
import com.gonzazago.nauta.container.repository.container.ContainerRepository
import io.vertx.core.impl.logging.LoggerFactory

class ContainerService(private val repository: ContainerRepository) {

    private val log = LoggerFactory.getLogger(ContainerService::class.java)

    suspend fun createContainer(container: Container) {
        try {
            val existingContainer = repository.getContainerByID(container.container)
            if (existingContainer != null) {
                val mergedAssociatedOrderIds = (existingContainer.associatedOrders ?: emptyList()) +
                        (container.associatedOrders ?: emptyList())
                val distinctMergedAssociatedOrderIds = mergedAssociatedOrderIds.distinct()

                val updateContainer = container.copy(
                    clientId = existingContainer.clientId,
                    associatedOrders = distinctMergedAssociatedOrderIds
                )
                repository.save(updateContainer)
            } else {
                repository.save(container)
            }

        } catch (e: Exception) {
            log.error("Error try save container", e)
        }

    }

    suspend fun getContainerByClient(clientID: String): List<Container> {
        try {
            return repository.getContainerByClient(clientID)
        } catch (e: Exception) {
            log.error("Error try get container for client: $clientID", e)
            throw ContainerServiceException(
                "Error try get container for client",
                PERSISTENCE_ERROR, e
            )
        }

    }

    suspend fun getOrdersByContainer(containerID: String, clientID: String): List<AssociatedOrder>? {

        val container = repository.getContainerByID(containerID)

        if (container?.associatedOrders != null) {
            return container
                .associatedOrders.stream().filter { it.clientId == clientID }?.toList()
        }
        return emptyList()

    }
}