package com.gonzazago.nauta.orchestator.routes

import com.gonzazago.nauta.orchestator.delivery.rest.IngestHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class IngestRouter(private val ingestHandler: IngestHandler) {

    fun ingestRoute(vertx: Vertx): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.post("/email").handler(ingestHandler::handleEmailIngestion)

        return router
    }
}