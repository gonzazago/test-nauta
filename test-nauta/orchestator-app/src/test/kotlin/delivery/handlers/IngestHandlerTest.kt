package delivery.handlers


import com.gonzazago.nauta.orchestator.application.container.ProcessContainerAction
import com.gonzazago.nauta.orchestator.application.order.ProcessOrderAction
import com.gonzazago.nauta.orchestator.delivery.rest.IngestHandler
import io.mockk.*
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest
import org.koin.test.inject

@OptIn(ExperimentalCoroutinesApi::class)
internal class IngestHandlerTest : KoinTest {

    private val handler by inject<IngestHandler>()
    private val processContainerAction by inject<ProcessContainerAction>()
    private val processOrderAction by inject<ProcessOrderAction>()
    private lateinit var context: RoutingContext


    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        clearAllMocks()
    }


    @Test
    fun `should handle email ingestion and delegate to ProcessOrderAction`() = runTest(TestKoin.getDispatcher()) {
        val context = mockk<RoutingContext>()
        val requestBody = """
            {
              "booking": "BK123",
              "containers": [
                { "container": "MEDU1234567" }
              ],
              "orders": [
                {
                  "purchase": "PO123",
                  "invoices": [
                    { "invoice": "IN123" }
                  ]
                }
              ]
            }
        """.trimIndent()
        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns "test-client-id"
        }

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response


        handler.handleEmailIngestion(context)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            processOrderAction.processOrder(any())
        }

        coVerify(exactly = 1) {
            processContainerAction.processContainer(any())
        }

    }

    @Test
    fun `should handle email ingestion only container and delegate to ProcessContainerAction`() = runTest(TestKoin.getDispatcher()) {
        val context = mockk<RoutingContext>()
        val requestBody = """
            {
              "booking": "BK123",
              "containers": [
                { "container": "MEDU1234567" }
              ]
            }
        """.trimIndent()
        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns "test-client-id"
        }

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response


        handler.handleEmailIngestion(context)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            processOrderAction.processOrder(any())
        }

        coVerify(exactly = 1) {
            processContainerAction.processContainer(any())
        }

    }


    @Test
    fun `should handle email ingestion only order and delegate to ProcessOrderAction`() = runTest(TestKoin.getDispatcher()) {
        val context = mockk<RoutingContext>()
        val requestBody = """
            {
              "booking": "BK123",
              "orders": [
                {
                  "purchase": "PO123",
                  "invoices": [
                    { "invoice": "IN123" }
                  ]
                }
              ]
            }
        """.trimIndent()
        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns "test-client-id"
        }

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response


        handler.handleEmailIngestion(context)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            processOrderAction.processOrder(any())
        }

        coVerify(exactly = 0) {
            processContainerAction.processContainer(any())
        }

    }


    @Test
    fun `should handle email ingestion and fail userID without`() = runTest(TestKoin.getDispatcher()) {
        val context = mockk<RoutingContext>()
        val requestBody = """
            {
              "booking": "BK123",
              "containers": [
                { "container": "MEDU1234567" }
              ],
              "orders": [
                {
                  "purchase": "PO123",
                  "invoices": [
                    { "invoice": "IN123" }
                  ]
                }
              ]
            }
        """.trimIndent()
        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns null
        }

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response


        handler.handleEmailIngestion(context)
        advanceUntilIdle()

        val slot = slot<String>()
        verify {
            response.setStatusCode(400)
            response.end(capture(slot))
        }

        assert(slot.captured.contains("Client ID header missing"))


        coVerify(exactly = 0) {
            processOrderAction.processOrder(any())
        }

        coVerify(exactly = 0) {
            processContainerAction.processContainer(any())
        }

    }

    @Test
    fun `should handle email ingestion and fail booking without`() = runTest(TestKoin.getDispatcher()) {
        val context = mockk<RoutingContext>()
        val requestBody = """
            {
              "containers": [
                { "container": "MEDU1234567" }
              ],
              "orders": [
                {
                  "purchase": "PO123",
                  "invoices": [
                    { "invoice": "IN123" }
                  ]
                }
              ]
            }
        """.trimIndent()
        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns "1234"
        }

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response


        handler.handleEmailIngestion(context)
        advanceUntilIdle()

        val slot = slot<String>()
        verify {
            response.setStatusCode(400)
            response.end(capture(slot))
        }

        assert(slot.captured.contains("Booking ID missing"))


        coVerify(exactly = 0) {
            processOrderAction.processOrder(any())
        }

        coVerify(exactly = 0) {
            processContainerAction.processContainer(any())
        }

    }

    @Test
    fun `should return 202 if sendContainer throws exception`() = runTest(TestKoin.getDispatcher()) {
        val requestBody = """
        {
          "booking": "BK123",
          "containers": [{ "container": "MEDU1234567" }]
        }
    """.trimIndent()

        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns "test-client-id"
        }

        // Mock response relajado pero mockeamos setStatusCode y end para verificar llamados
        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response

        coEvery { processContainerAction.processContainer(any()) } throws RuntimeException("Test exception")

        handler.handleEmailIngestion(context)

        advanceUntilIdle()

        verify { response.setStatusCode(202) }
        verify { response.end("Ingestion request accepted. Processing asynchronously via Event Bus.") }

    }


    @Test
    fun `should handle email return 400  fail marhsall`() = runTest(TestKoin.getDispatcher()) {
        val context = mockk<RoutingContext>()
        val requestBody = """
            {             }
        """.trimIndent()
        val request = mockk<HttpServerRequest> {
            every { getHeader("X-Client-ID") } returns null
        }

        val response = mockk<HttpServerResponse>(relaxed = true) {
            every { setStatusCode(any()) } returns this
            every { end(any<String>()) } returns mockk()
        }

        every { context.body().asString() } returns requestBody
        every { context.request() } returns request
        every { context.response() } returns response


        handler.handleEmailIngestion(context)
        advanceUntilIdle()

        val slot = slot<String>()
        verify {
            response.setStatusCode(400)
            response.end(capture(slot))
        }

        assert(slot.captured.contains("Invalid ingestion data"))


        coVerify(exactly = 0) {
            processOrderAction.processOrder(any())
        }

        coVerify(exactly = 0) {
            processContainerAction.processContainer(any())
        }

    }


    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll(): Unit {
            TestKoin.startTestKoin()
        }

        @JvmStatic
        @AfterAll
        fun afterAll(): Unit {
            TestKoin.stopTestKoin()
        }
    }


}