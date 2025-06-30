package com.gonzazago.nauta.container.routes

import com.gonzazago.nauta.container.delivery.rest.GetContainerByClientHandler
import com.gonzazago.nauta.container.delivery.rest.GetOrdersByContainerHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

class ContainerRouter(
    private val getContainerByClientHandler: GetContainerByClientHandler,
    private val getOrdersByContainer: GetOrdersByContainerHandler
) {
    fun containerRouter(vertx: Vertx): Router {
        val router = Router.router(vertx)
        router.get("/").handler(getContainerByClientHandler::getContainerByClientHandler)
        router.get("/:containerId/orders").handler(getOrdersByContainer::getOrdersByContainerHandler)
        return router
    }
}