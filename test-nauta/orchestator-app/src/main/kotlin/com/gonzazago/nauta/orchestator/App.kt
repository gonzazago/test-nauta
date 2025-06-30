package com.gonzazago.nauta.orchestator

import com.gonzazago.nauta.container.ContainerMain
import com.gonzazago.nauta.orchestator.h2console.H2Console
import com.gonzazago.nauta.orchestator.modules.ModuleLoader
import com.gonzazago.nauta.orders.OrderMain
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(App::class.java.name) { res ->
        if (res.succeeded()) {
            LoggerFactory.getLogger("MainApp").info("Main application Verticle deployed successfully.")
        } else {
            LoggerFactory.getLogger("MainApp").error("Failed to deploy Main application Verticle!", res.cause())
        }
    }
}

class App : AbstractVerticle(), KoinComponent {
    private val log = LoggerFactory.getLogger(App::class.java)
    private val server: Server by inject() // Este es el Server que arrancará el HTTP

    override fun start(startPromise: Promise<Void>) {
        log.info("Starting main application Verticle (ContainerApp.kt orchestrator)...")
        ModuleLoader.init(vertx)

        server.start()

        deployServiceVerticle(OrderMain::class.java.name, "OrderService")
        deployServiceVerticle(ContainerMain::class.java.name, "ContainerService")

        vertx.deployVerticle(H2Console::class.java.name) { res ->
            if (res.succeeded()) {
                log.info("H2 Console Verticle deployed successfully.")
            } else {
                log.error("H2 Console Verticle failed to deploy!", res.cause())
            }
        }

        startPromise.complete()
    }

    private fun deployServiceVerticle(verticleName: String, serviceName: String) {
        vertx.deployVerticle(verticleName) { res -> /* ... lógica de despliegue ... */ }
    }

}