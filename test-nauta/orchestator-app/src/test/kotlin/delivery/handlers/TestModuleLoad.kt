package delivery.handlers

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.gonzazago.nauta.container.delivery.rest.GetContainerByClientHandler
import com.gonzazago.nauta.orchestator.application.container.ProcessContainerAction
import com.gonzazago.nauta.orchestator.application.order.ProcessOrderAction
import com.gonzazago.nauta.orchestator.delivery.rest.IngestHandler
import com.gonzazago.nauta.orders.application.usecase.orders.GetContainerByPurchaseIDUseCase
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest

object TestKoin : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    fun getDispatcher() = testDispatcher
    fun getScope(): CoroutineScope = testScope

    fun startTestKoin() {
        startKoin {
            modules(testModules())
        }
    }

    fun stopTestKoin() {
        stopKoin()
    }

    private fun testModules(): List<Module> = listOf(
        module {
            single<CoroutineScope> { testScope }

            single {
                ObjectMapper().apply {
                    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                    setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    registerModule(KotlinModule.Builder().build())
                    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                }
            }

            single { mockk<ProcessOrderAction>(relaxed = true) }
            single { mockk<ProcessContainerAction>(relaxed = true) }
            single { mockk<GetContainerByPurchaseIDUseCase>(relaxed = true) }

            single { IngestHandler(get(), get(), get(), get()) }
            single { GetContainerByClientHandler(get(), get()) }
        }
    )
}
