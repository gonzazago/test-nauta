package com.gonzazago.nauta.container.repository.container

import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.mapper.ContainerMapper
import com.gonzazago.nauta.container.repository.entity.ContainerEntity
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.coAwait

class ContainerRepositoryImpl(
    private val containerMapper: ContainerMapper,
    private val mongoClient: MongoClient
) : ContainerRepository {

    override suspend fun save(container: Container): Container {

        val entity = containerMapper.toEntity(container)
        val document = containerMapper.toMongoDocument(entity)
        LOG.info("container to save ${document.encodePrettily()}")

        try {
            val result = mongoClient.save(COLLECTION_NAME, document).coAwait()
            return container
        } catch (e: Exception) {
            LOG.error("ContainerRepositoryImpl: Failed to save container ${container.container}: ${e.message}", e)
            throw RuntimeException("ContainerRepositoryImpl: Failed to save container: ${e.message}", e)
        }
    }

    override suspend fun getContainerByClient(clientId: String): List<Container> {
        val query = JsonObject()
            .put(CLIENT_ID, clientId)
        val documents = mongoClient.find(COLLECTION_NAME, query).coAwait()
        return documents.map {
            val entity = it.mapTo(ContainerEntity::class.java)
            containerMapper.toDomainModel(entity)
        }

    }

    override suspend fun getContainerByID(containerID: String): Container? {
        val query = JsonObject()
            .put(ID, containerID)
        val document = mongoClient.findOne(COLLECTION_NAME, query, JsonObject()).coAwait()
        return document?.let {
            val entity = it.mapTo(ContainerEntity::class.java)
            containerMapper.toDomainModel(entity)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ContainerRepositoryImpl::class.java)
        private const val CLIENT_ID = "client_id"
        private const val ID = "_id"
        private const val COLLECTION_NAME = "containers"
    }
}


