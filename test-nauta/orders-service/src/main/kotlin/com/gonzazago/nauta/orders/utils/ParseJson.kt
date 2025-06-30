package com.gonzazago.nauta.orders.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue


val snakeCaseMapper: ObjectMapper = ObjectMapper()
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    .registerModule(KotlinModule.Builder().build())
    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

val camelCaseMapper: ObjectMapper = ObjectMapper()
    .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
    .registerModule(KotlinModule.Builder().build())
    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)


inline fun <reified T> String.parse(mapper: ObjectMapper = snakeCaseMapper): T = mapper.readValue(this)

inline fun <reified T> JsonNode.parse(mapper: ObjectMapper = snakeCaseMapper): T =
    mapper.treeToValue(this, T::class.java)

inline fun <reified T> T.asMap(mapper: ObjectMapper = snakeCaseMapper): Map<String, Any> =
    mapper.convertValue(this, object : TypeReference<Map<String, Any>>() {})

fun Any.toJSON(mapper: ObjectMapper = snakeCaseMapper): String = mapper.writeValueAsString(this)

