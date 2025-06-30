package com.gonzazago.nauta.orchestator.modules

import com.gonzazago.nauta.container.application.usecase.CreateContainer
import com.gonzazago.nauta.container.application.usecase.GetContainerByClient
import com.gonzazago.nauta.container.application.usecase.GetOrdersByContainer
import com.gonzazago.nauta.orchestator.application.container.ProcessContainerAction
import com.gonzazago.nauta.orchestator.application.order.ProcessOrderAction
import com.gonzazago.nauta.orders.application.usecase.orders.CreateOrder
import com.gonzazago.nauta.orders.application.usecase.orders.GetContainerByPurchaseIDUseCase
import org.koin.core.module.Module

fun Module.useCases() {
    //Orchestrator-service
    single { ProcessOrderAction(get()) }
    single { ProcessContainerAction(get()) }
    //Order-service
    single { CreateOrder(get()) }
    single { GetContainerByPurchaseIDUseCase(get()) }
    //ContainerService
    single { CreateContainer(get()) }
    single { GetContainerByClient(get()) }
    single { GetOrdersByContainer(get()) }
}
