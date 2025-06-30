package com.gonzazago.nauta.container.repository

import com.gonzazago.nauta.container.domain.model.AssociatedOrder
import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.mapper.ContainerMapper
import com.gonzazago.nauta.container.repository.container.ContainerRepositoryImpl
import com.gonzazago.nauta.container.repository.entity.AssociatedOrderEntity
import com.gonzazago.nauta.container.repository.entity.ContainerEntity
import io.mockk.*
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContainerRepositoryTest {
    private val mongoClient = mockk<MongoClient>()
    private val mapper = mockk<ContainerMapper>()
    private lateinit var repository: ContainerRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        repository = ContainerRepositoryImpl(mapper, mongoClient)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `save should call mongoClient and return container`() = runTest(testDispatcher) {

        val entity = ContainerEntity("C2", "client1", "B2", listOf(AssociatedOrderEntity("client1", "O1")))
        val container = Container("C1", "client1", "B1", listOf(AssociatedOrder("client1", "O1")))

        val document = JsonObject(mapOf("_id" to "C1", "client_id" to "client1", "booking_id" to "B1"))

        every { mapper.toEntity(container) } returns entity
        every { mapper.toMongoDocument(entity) } returns document
        coEvery { mongoClient.save("containers", document) } returns Future.succeededFuture("C1")

        val result = repository.save(container)

        assertEquals(container, result)
        coVerify { mongoClient.save("containers", document) }
    }

    @Test
    fun `getContainerByID should return container when found`() = runTest(testDispatcher) {
        val clientId = "client1"
        val containerId = "C1"
        val jsonMock = mockk<JsonObject>()
        val entity = ContainerEntity("C2", "client1", "B2", listOf(AssociatedOrderEntity(clientId, "O1")))
        val domain = Container("C1", "client1", "B1", listOf(AssociatedOrder(clientId, "O1")))

        coEvery {
            mongoClient.findOne(
                eq("containers"),
                match { it.getString("_id") == containerId },
                any()
            )
        } returns Future.succeededFuture(jsonMock)

        every { jsonMock.mapTo(ContainerEntity::class.java) } returns entity
        every { mapper.toDomainModel(entity) } returns domain

        val result = repository.getContainerByID(containerId)

        assertEquals(domain, result)
    }

    @Test
    fun `getContainerByID should return null when not found`() = runTest(testDispatcher) {
        coEvery {
            mongoClient.findOne("containers", any(), any())
        } returns Future.succeededFuture<JsonObject?>(null)

        val result = repository.getContainerByID("C1")

        assertNull(result)
    }

    @Test
    fun `getContainerByClient should return list of containers`() = runTest(testDispatcher) {
        val jsonMock1 = mockk<JsonObject>()
        val jsonMock2 = mockk<JsonObject>()

        val clientID = "C1"
        val orderID = "O2"

        val entity1 = ContainerEntity("C1", "client1", "B1", listOf(AssociatedOrderEntity(clientID, orderID)))
        val entity2 = ContainerEntity("C2", "client1", "B2", listOf(AssociatedOrderEntity(clientID, "O2")))

        val domain1 = Container("C1", "client1", "B1", listOf(AssociatedOrder(clientID, orderID)))
        val domain2 = Container("C2", "client1", "B2", listOf(AssociatedOrder(clientID, "O2")))

        coEvery {
            mongoClient.find("containers", match { it.getString("client_id") == "client1" })
        } returns Future.succeededFuture(listOf(jsonMock1, jsonMock2))

        every { jsonMock1.mapTo(ContainerEntity::class.java) } returns entity1
        every { jsonMock2.mapTo(ContainerEntity::class.java) } returns entity2

        every { mapper.toDomainModel(entity1) } returns domain1
        every { mapper.toDomainModel(entity2) } returns domain2

        val result = repository.getContainerByClient("client1")

        assertEquals(listOf(domain1, domain2), result)
    }
}