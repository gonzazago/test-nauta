package com.gonzazago.nauta.orders.delivery.rest.orders

import com.gonzazago.nauta.orders.application.usecase.orders.GetContainerByPurchaseIDUseCase
import com.gonzazago.nauta.orders.execption.OrderServiceException
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GetContainerByPurchaseIDHandler(
    private val getContainerByPurchaseIDUseCase: GetContainerByPurchaseIDUseCase,
    private val coroutineScope: CoroutineScope
) {

    fun getContainerByPurchaseID(ctx: RoutingContext) {
        val purchaseId = ctx.pathParam("purchaseId")
        val clientId = ctx.request().getHeader("X-Client-ID") ?: run {
            ctx.response().setStatusCode(400).end("Client ID header missing."); return
        }

        coroutineScope.launch {
            try {
                val containerIds = getContainerByPurchaseIDUseCase.execute(purchaseId, clientId)

                if (containerIds.isEmpty()) {
                    ctx.response().setStatusCode(404).end("No containers found for order $purchaseId.")
                    return@launch
                }
                val response = JsonObject().put("order_id", purchaseId).put("containers", containerIds)
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(response.encodePrettily()) // Serializa la lista de DTOs de Container
            } catch (e: OrderServiceException) {
                ctx.response().setStatusCode(404).end(e.message) // Por ejemplo, si la orden no existe
            } catch (e: Exception) {
                ctx.response().setStatusCode(500).end("Internal Server Error: ${e.message}")
            }
        }

    }
}