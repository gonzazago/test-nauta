package com.gonzazago.nauta.container.delivery

import com.gonzazago.nauta.container.application.usecase.GetContainerByClient
import com.gonzazago.nauta.container.delivery.rest.GetContainerByClientHandler
import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.execption.ContainerServiceException
import io.mockk.*
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetContainerByClientHandlerTest {

    private val getContainerByClient = mockk<GetContainerByClient>()
    private val routingContext = mockk<RoutingContext>(relaxed = true)
    private val request = mockk<HttpServerRequest>(relaxed = true)
    private val response = mockk<HttpServerResponse>(relaxed = true)
    private val testScope = TestScope(StandardTestDispatcher())
    private lateinit var handler: GetContainerByClientHandler

    @BeforeEach
    fun setUp() {
        handler = GetContainerByClientHandler(getContainerByClient, testScope)
        every { routingContext.request() } returns request
        every { routingContext.response() } returns response
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should return containers when client is valid`() = runTest {
        val clientId = "clientA"
        val container = Container(
            clientId = clientId,
            container = "C1",
            bookingId = "B1",
            associatedOrders = emptyList()
        )
        val containerList = listOf(container)

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk(relaxed = true)
        }

        every { request.getHeader("X-Client-ID") } returns clientId
        every { response.putHeader(any<String>(), any<String>()) } returns response
        coEvery { getContainerByClient.execute(clientId) } returns containerList

        every { routingContext.request() } returns request
        every { routingContext.response() } returns response

        // Call handler
        handler.getContainerByClientHandler(routingContext)

        // Ejecutar coroutines
        testScope.advanceUntilIdle()

        coVerify(exactly = 1) { getContainerByClient.execute(clientId) }
        verify {
            response.putHeader("content-type", "application/json")
            response.setStatusCode(200)
            response.end(match<String> { it.contains("C1") && it.contains(clientId) })
        }
    }

    @Test
    fun `should return bad request when client is missing`() = runTest {

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk(relaxed = true)
        }

        every { request.getHeader("X-Client-ID") } returns null
        every { response.putHeader(any<String>(), any<String>()) } returns response

        every { routingContext.request() } returns request
        every { routingContext.response() } returns response

        // Call handler
        handler.getContainerByClientHandler(routingContext)

        // Ejecutar coroutines
        testScope.advanceUntilIdle()

        coVerify(exactly = 0) { getContainerByClient.execute(any()) }
        verify {
            response.setStatusCode(400)
            response.end(match<String> { it.contains("Client ID header missing.") })
        }
    }

    @Test
    fun `should return not_found when not exist container for client`() = runTest {

        val clientId = "clientA"
        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk(relaxed = true)
        }

        every { request.getHeader("X-Client-ID") } returns clientId
        every { response.putHeader(any<String>(), any<String>()) } returns response
        coEvery { getContainerByClient.execute(clientId) } returns emptyList()

        every { routingContext.request() } returns request
        every { routingContext.response() } returns response

        // Call handler
        handler.getContainerByClientHandler(routingContext)

        // Ejecutar coroutines
        testScope.advanceUntilIdle()

        coVerify(exactly = 1) { getContainerByClient.execute(clientId) }
        verify {
            response.putHeader("content-type", "application/json")
            response.setStatusCode(404)
            response.end(match<String> { it.contains("No containers found for client $clientId.") })
        }
    }

    @Test
    fun `should return internal_error when use case fail`() = runTest {

        val clientId = "clientA"
        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk(relaxed = true)
        }

        every { request.getHeader("X-Client-ID") } returns clientId
        every { response.putHeader(any<String>(), any<String>()) } returns response
        coEvery { getContainerByClient.execute(clientId) } throws  ContainerServiceException(
            "Error try get container for clien", ContainerServiceException.PERSISTENCE_ERROR
        )

        every { routingContext.request() } returns request
        every { routingContext.response() } returns response

        // Call handler
        handler.getContainerByClientHandler(routingContext)

        // Ejecutar coroutines
        testScope.advanceUntilIdle()

        coVerify(exactly = 1) { getContainerByClient.execute(clientId) }
        verify {
            response.putHeader("content-type", "application/json")
            response.setStatusCode(500)
            response.end(match<String> { it.contains("Internal Server Error:") })
        }
    }
}
