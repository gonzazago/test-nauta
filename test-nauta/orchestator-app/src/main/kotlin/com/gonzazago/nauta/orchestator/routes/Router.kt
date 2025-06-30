package com.gonzazago.nauta.orchestator.routes

import com.gonzazago.nauta.container.routes.ContainerRouter
import com.gonzazago.nauta.orders.OrderMain
import com.gonzazago.nauta.orders.routes.OrderRouter
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Router


class Router(
    private val vertx: Vertx,
    private val ingestRouter: IngestRouter,
    private val orderRouter: OrderRouter,
    private val containerRouter: ContainerRouter,
) {
    private val log = LoggerFactory.getLogger(Router::class.java)
    fun createRouter(vertx: Vertx): Router {
        val router = Router.router(vertx)

        router.get("/health").handler { ctx ->
            ctx.response()
                .putHeader("content-type", "text/plain")
                .end("OK")
        }
        router.mountSubRouter("/v1/api", ingestRouter.ingestRoute(vertx))

        log.info("MainRouter: Attempting to mount OrderRouter under /v1/api/orders.")
        router.mountSubRouter("/v1/api/orders", orderRouter.ordersRoutes(vertx))
        log.info("MainRouter: Successfully mounted OrderRouter under /v1/api/orders.")
        router.mountSubRouter("/v1/api/containers", containerRouter.containerRouter(vertx))

        router.route().handler { ctx ->
            log.warn("MainRouter: No route found for request: ${ctx.request().method()} ${ctx.request().uri()}")
            ctx.response().setStatusCode(404).end("Not Found (via MainRouter fallback)")
        }

        return router
    }
}
