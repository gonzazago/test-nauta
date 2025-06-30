package com.gonzazago.nauta.orchestator.modules

import com.gonzazago.nauta.container.domain.service.ContainerService
import com.gonzazago.nauta.orders.domain.services.OrderService
import org.koin.core.module.Module

fun Module.services() {
    single { OrderService(get()) }
    single { ContainerService(get()) }
}