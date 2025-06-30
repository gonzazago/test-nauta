package com.gonzazago.nauta.orchestator.modules

import com.gonzazago.nauta.orchestator.infra.publisher.EventBusPublisher
import com.gonzazago.nauta.orchestator.infra.publisher.Publisher
import com.gonzazago.nauta.orders.domain.model.Order
import org.koin.core.module.Module

fun Module.publisher() {

    single<Publisher<Order>> { EventBusPublisher(get(), get()) }
}