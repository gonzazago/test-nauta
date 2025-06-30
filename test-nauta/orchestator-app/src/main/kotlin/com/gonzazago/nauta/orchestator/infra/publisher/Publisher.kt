package com.gonzazago.nauta.orchestator.infra.publisher

interface Publisher<T> {

    suspend fun publish(topic: String, message: T)
}