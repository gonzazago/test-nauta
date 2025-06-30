package com.gonzazago.nauta.container.delivery.rest

import com.gonzazago.nauta.container.application.usecase.GetOrdersByContainer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GetOrdersByContainerHandler(
    private val getOrdersByContainer: GetOrdersByContainer,
    private val coroutineScope: CoroutineScope
) {

    fun getOrdersByContainerHandler(ctx: RoutingContext) {
        lateinit var response: JsonObject
        val containerID = ctx.pathParam("containerId")
        val clientId = ctx.request().getHeader("X-Client-ID") ?: run {
            response = JsonObject().put("status_code", 400).put("message", "Client ID header missing.")
            ctx.response()
                .setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(response.encodePrettily()); return
        }
        coroutineScope.launch {
            try {
                val orders = getOrdersByContainer.getOrderByContainer(containerID, clientId)

                if(orders.isNullOrEmpty()){
                    response =
                        JsonObject()
                            .put("status_code", 404)
                            .put("message", "No order into containers for client $clientId.")
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(404)
                        .end(response.encodePrettily())
                    return@launch
                }

                response = JsonObject()
                    .put("client_id", clientId)
                    .put("orders", orders)
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(response.encodePrettily())

            } catch (e: Exception) {
                response = JsonObject().put("status_code", 500)
                    .put("message", "Internal Server Error: ${e.message}")
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(response.encodePrettily())
            }
        }
    }
}