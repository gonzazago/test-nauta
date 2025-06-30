package com.gonzazago.nauta.container.repository.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class ContainerEntity(
    @JsonProperty("_id")
    val id: String,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("booking_id")
    val bookingId: String?,
    @JsonProperty("associated_order_ids")
    val associatedOrders: List<AssociatedOrderEntity>? = null,
)


data class AssociatedOrderEntity(
    @JsonProperty("order_id")
    val orderId: String,
    @JsonProperty("client_id")
    val clientId: String
)