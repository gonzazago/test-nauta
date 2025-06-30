package com.gonzazago.nauta.container.repository.container

import com.gonzazago.nauta.container.domain.model.Container

interface ContainerRepository {
    suspend fun save(container: Container): Container
    suspend fun getContainerByID(containerID: String): Container?
    suspend fun getContainerByClient(clientId: String): List<Container>
}