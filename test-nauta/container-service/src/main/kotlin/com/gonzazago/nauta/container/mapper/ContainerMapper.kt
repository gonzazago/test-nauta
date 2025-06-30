package com.gonzazago.nauta.container.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.gonzazago.nauta.container.delivery.dto.AssociatedOrderDTO
import com.gonzazago.nauta.container.delivery.dto.ContainerResponseDto
import com.gonzazago.nauta.container.domain.model.AssociatedOrder
import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.repository.entity.AssociatedOrderEntity
import com.gonzazago.nauta.container.repository.entity.ContainerEntity
import io.vertx.core.json.JsonObject


class ContainerMapper(
    private val objectMapper: ObjectMapper
) {

    fun toEntity(model: Container): ContainerEntity {
        return ContainerEntity(
            id = model.container,
            clientId = model.clientId,
            bookingId = model.bookingId,
            associatedOrders = model.associatedOrders?.map {
                AssociatedOrderEntity(it.orderId, it.clientId)
            }?.toList()
        )
    }

    fun toDomainModel(entity: ContainerEntity): Container {
        return Container(
            container = entity.id,
            clientId = entity.clientId,
            bookingId = entity.bookingId,
            associatedOrders = entity.associatedOrders?.map {
                AssociatedOrder(it.orderId, it.clientId)
            }?.toList()
        )
    }

    fun toResponseDto(model: Container): ContainerResponseDto {
        return ContainerResponseDto(
            id = model.container,
            clientId = model.clientId,
            bookingId = model.bookingId,
            associatedOrders = model.associatedOrders?.map {
                AssociatedOrderDTO(it.orderId, it.clientId)
            }?.toList()
        )
    }

    fun toMongoDocument(entity: ContainerEntity): JsonObject {
        val entityJsonString = objectMapper.writeValueAsString(entity)
        return JsonObject(entityJsonString)
    }
}