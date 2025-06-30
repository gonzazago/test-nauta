package com.gonzazago.nauta.container.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.gonzazago.nauta.container.delivery.dto.AssociatedOrderDTO
import com.gonzazago.nauta.container.domain.model.AssociatedOrder
import com.gonzazago.nauta.container.domain.model.Container
import com.gonzazago.nauta.container.repository.entity.AssociatedOrderEntity
import com.gonzazago.nauta.container.repository.entity.ContainerEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContainerMapperTest {
    private lateinit var mapper: ContainerMapper
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    fun setup() {
        mapper = ContainerMapper(objectMapper)
    }

    @Test
    fun `toEntity maps Container to ContainerEntity correctly`() {
        val container = Container(
            container = "C1",
            clientId = "client1",
            bookingId = "B1",
            associatedOrders = listOf(AssociatedOrder("O1", "client1"))
        )

        val entity = mapper.toEntity(container)

        assertEquals("C1", entity.id)
        assertEquals("client1", entity.clientId)
        assertEquals("B1", entity.bookingId)
        assertEquals(listOf(AssociatedOrderEntity("O1", "client1")), entity.associatedOrders)
    }

    @Test
    fun `toDomainModel maps ContainerEntity to Container correctly`() {
        val entity = ContainerEntity(
            id = "C1",
            clientId = "client1",
            bookingId = "B1",
            associatedOrders = listOf(AssociatedOrderEntity("O1", "client1"))
        )

        val domain = mapper.toDomainModel(entity)

        assertEquals("C1", domain.container)
        assertEquals("client1", domain.clientId)
        assertEquals("B1", domain.bookingId)
        assertEquals(listOf(AssociatedOrder("O1", "client1")), domain.associatedOrders)
    }

    @Test
    fun `toResponseDto maps Container to ContainerResponseDto correctly`() {
        val container = Container(
            container = "C1",
            clientId = "client1",
            bookingId = "B1",
            associatedOrders = listOf(AssociatedOrder("O1", "client1"))
        )

        val dto = mapper.toResponseDto(container)

        assertEquals("C1", dto.id)
        assertEquals("client1", dto.clientId)
        assertEquals("B1", dto.bookingId)
        assertEquals(listOf(AssociatedOrderDTO("O1", "client1")), dto.associatedOrders)
    }

    @Test
    fun `toMongoDocument converts ContainerEntity to JsonObject correctly`() {
        val entity = ContainerEntity(
            id = "C1",
            clientId = "client1",
            bookingId = "B1",
            associatedOrders = listOf(AssociatedOrderEntity("O1", "client1"))
        )

        val jsonObject = mapper.toMongoDocument(entity)

        assertEquals("C1", jsonObject.getString("_id"))
        assertEquals("client1", jsonObject.getString("client_id"))
        assertEquals("B1", jsonObject.getString("booking_id"))

        val associatedOrderIdsArray = jsonObject.getJsonArray("associated_order_ids")


        assertEquals(associatedOrderIdsArray.size(), 1)
    }
}