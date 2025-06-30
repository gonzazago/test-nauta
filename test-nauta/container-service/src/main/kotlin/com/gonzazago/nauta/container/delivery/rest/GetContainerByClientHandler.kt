package com.gonzazago.nauta.container.delivery.rest

import com.gonzazago.nauta.container.application.usecase.GetContainerByClient
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GetContainerByClientHandler(
    private val getContainerByClient: GetContainerByClient,
    private val coroutineScope: CoroutineScope

) {

    fun getContainerByClientHandler(ctx: RoutingContext) {
        lateinit var response: JsonObject
        val clientId = ctx.request().getHeader("X-Client-ID") ?: run {
            response = JsonObject().put("status_code", 400).put("message", "Client ID header missing.")
            ctx.response()
                .setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(response.encodePrettily()); return
        }

        coroutineScope.launch {

            try {
                val containerIds = getContainerByClient.execute(clientId)

                if (containerIds.isEmpty()) {
                    response =
                        JsonObject().put("status_code", 404).put("message", "No containers found for client $clientId.")
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(404)
                        .end(response.encodePrettily())
                    return@launch
                }
                response = JsonObject().put("client_id", clientId).put("containers", containerIds)
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(200)
                    .end(response.encodePrettily())
            } catch (e: Exception) {
                response = JsonObject().put("status_code", 500).put("message", "Internal Server Error: ${e.message}")
                ctx.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(500)
                    .end(response.encodePrettily())
            }
        }
    }

}