package com.gonzazago.nauta.orchestator.modules

import com.gonzazago.nauta.container.application.consumer.ContainerWorker
import com.gonzazago.nauta.orders.application.consumer.OrderWorker
import org.koin.core.module.Module

fun Module.workers() {
    single { OrderWorker(get()) }
    single { ContainerWorker(get()) }
}