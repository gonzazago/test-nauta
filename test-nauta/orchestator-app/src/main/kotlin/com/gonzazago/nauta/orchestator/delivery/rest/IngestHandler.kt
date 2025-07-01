package com.gonzazago.nauta.orchestator.delivery.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gonzazago.nauta.orchestator.application.container.ProcessContainerAction
import com.gonzazago.nauta.orchestator.application.order.ProcessOrderAction
import com.gonzazago.nauta.orchestator.delivery.dto.AssociatedOrderDTO
import com.gonzazago.nauta.orchestator.delivery.dto.ContainerMessageDTO
import com.gonzazago.nauta.orchestator.delivery.dto.EmailIngestRequest
import com.gonzazago.nauta.orders.domain.model.Invoice
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class IngestHandler(
    private val objectMapper: ObjectMapper,
    private val processOrderAction: ProcessOrderAction,
    private val processContainerAction: ProcessContainerAction,
    private val coroutineScope: CoroutineScope
) {

    val log = LoggerFactory.getLogger(IngestHandler::class.java)

    fun handleEmailIngestion(ctx: RoutingContext) {
        val rawJsonString = ctx.body().asString()

        try {
            val emailRequestDto = objectMapper.readValue<EmailIngestRequest>(rawJsonString)

            val clientId =
                ctx.request().getHeader("X-Client-ID") ?: throw IllegalArgumentException("Client ID header missing")
            val bookingId = emailRequestDto.booking ?: throw IllegalArgumentException("Booking ID missing")

            ctx.response()
                .setStatusCode(202)
                .end("Ingestion request accepted. Processing asynchronously via Event Bus.")

            coroutineScope.launch {
                try {
                    if (emailRequestDto.orders != null) {
                        sendOrder(emailRequestDto, clientId, bookingId)
                    }
                    if (emailRequestDto.containers != null) {
                        sendContainer(emailRequestDto, clientId, bookingId)
                    }
                } catch (e: Exception) {
                    log.error("Central Ingester: Error during ingestion request: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            if (!ctx.response().ended()) {
                ctx.response().setStatusCode(400).end("Invalid ingestion data: ${e.message}")
            }
        }
    }


    private suspend fun sendOrder(
        emailIngestRequest: EmailIngestRequest,
        clientId: String,
        bookingId: String
    ) {
        val containerIds: List<String> = emailIngestRequest.containers?.map { it.container } ?: emptyList()


        emailIngestRequest.orders?.forEach { orderInputDto ->
            val invoiceDTO = orderInputDto.invoices
            val invoices = invoiceDTO.map {
                Invoice(
                    id = it.invoice,
                    orderPurchaseId = orderInputDto.purchase,
                    clientId = clientId
                )
            }
            val orderJsonForQueue = JsonObject.mapFrom(orderInputDto)
                .put("client_id", clientId)
                .put("booking_id", bookingId)
                .put("invoices", invoices)

            if (containerIds.isNotEmpty()) {
                orderJsonForQueue.put("container_ids", containerIds)

            }
            processOrderAction.processOrder(orderJsonForQueue)
        }
    }

    private suspend fun sendContainer(
        emailIngestRequest: EmailIngestRequest,
        clientId: String,
        bookingId: String
    ) {
        val associatedOrders: List<String> = emailIngestRequest.orders?.map { it.purchase } ?: emptyList()
        emailIngestRequest.containers?.forEach { containerInputDto ->
            lateinit var messageDTO: ContainerMessageDTO

            messageDTO = ContainerMessageDTO(
                container = containerInputDto.container,
                clientId = clientId,
                bookingId = bookingId,
                associatedOrders = associatedOrders.map { AssociatedOrderDTO(it, clientId) }
            )

            processContainerAction.processContainer(messageDTO)
        }
    }
}

