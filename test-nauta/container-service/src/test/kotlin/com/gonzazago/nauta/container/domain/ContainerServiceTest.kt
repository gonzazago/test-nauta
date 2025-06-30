package com.gonzazago.nauta.container.domain

import com.gonzazago.nauta.container.domain.model.AssociatedOrder
import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.domain.service.ContainerService
import com.gonzazago.nauta.container.execption.ContainerServiceException
import com.gonzazago.nauta.container.execption.ContainerServiceException.Companion.PERSISTENCE_ERROR
import com.gonzazago.nauta.container.repository.container.ContainerRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


@OptIn(ExperimentalCoroutinesApi::class)
class ContainerServiceTest {

    private val repository = mockk<ContainerRepository>()
    private lateinit var service: ContainerService
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        service = ContainerService(repository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `createContainer saves container when it does not exist`() = runTest(testDispatcher) {
        val clientID = "C1"
        val orderID = "O1"
        val container = Container("C1", "client1", "B1", listOf(AssociatedOrder(orderID, clientID)))

        coEvery { repository.getContainerByID("C1") } returns null
        coEvery { repository.save(container) } coAnswers { container }

        service.createContainer(container)

        coVerify(exactly = 1) { repository.save(container) }
    }

    @Test
    fun `createContainer merges associatedOrderIds if container exists`() = runTest(testDispatcher) {
        val clientID = "client1"
        val orderID1 = "O1"
        val orderID2 = "O2"
        val orderID3 = "O3"
        val orderID4 = "O4"

        val existing = Container(
            "C1", "client1", "B1", listOf(
                AssociatedOrder(orderID1, clientID),
                AssociatedOrder(orderID2, clientID)
            )
        )
        val incoming = Container(
            "C1", "client1", "B1",
            listOf(
                AssociatedOrder(orderID3, clientID),
                AssociatedOrder(orderID4, clientID)
            )
        )

        val expected = incoming.copy(
            associatedOrders = listOf(
                AssociatedOrder(orderID1, clientID),
                AssociatedOrder(orderID2, clientID),
                AssociatedOrder(orderID3, clientID),
                AssociatedOrder(orderID4, clientID)
            )
        )

        coEvery { repository.getContainerByID("C1") } returns existing
        coEvery { repository.save(expected) } coAnswers { expected }

        service.createContainer(incoming)

        coVerify { repository.save(expected) }
    }

    @Test
    fun `getContainerByClient returns list when successful`() = runTest(testDispatcher) {
        val clientID = "C1"
        val orderID1 = "O1"
        val containers = listOf(Container("C1", "client1", "B1", listOf(AssociatedOrder(clientID, orderID1))))
        coEvery { repository.getContainerByClient("client1") } returns containers

        val result = service.getContainerByClient("client1")

        assertEquals(containers, result)
    }

    @Test
    fun `getContainerByClient throws ContainerServiceException on failure`() = runTest(testDispatcher) {
        coEvery { repository.getContainerByClient("client1") } throws RuntimeException("DB down")

        val exception = assertThrows<ContainerServiceException> {
            service.getContainerByClient("client1")
        }

        assertEquals("Error try get container for client", exception.message)
        assertEquals(PERSISTENCE_ERROR, exception.errorCode)
    }
}