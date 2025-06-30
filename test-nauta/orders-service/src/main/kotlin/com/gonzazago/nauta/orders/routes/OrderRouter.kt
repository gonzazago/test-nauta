package com.gonzazago.nauta.orders.routes

import com.gonzazago.nauta.orders.delivery.rest.orders.GetContainerByPurchaseIDHandler
import com.gonzazago.nauta.orders.delivery.rest.orders.OrderHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class OrderRouter(
    private val orderHandler: OrderHandler,
    private val getContainerByPurchaseIDHandler: GetContainerByPurchaseIDHandler
) {

    fun ordersRoutes(vertx: Vertx): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.get("/:purchaseId/containers").handler(getContainerByPurchaseIDHandler::getContainerByPurchaseID)

        return router
    }
}
