package com.gonzazago.nauta.orchestator.h2console

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.impl.logging.LoggerFactory
import org.h2.tools.Server // <-- Importa la clase Server de H2

class H2Console : AbstractVerticle() {
    private val log = LoggerFactory.getLogger(H2Console::class.java)

    override fun start(startPromise: Promise<Void>) {
        try {
            // Iniciar el servidor web de H2
            // Esto levanta la consola H2 para TODAS las bases de datos H2 en memoria
            // que tu aplicación ha abierto.
            val webServer = Server.createWebServer(
                "-web", "-webAllowOthers", "-webDaemon", "-webPort", "8082" // Puerto para la consola H2
            ).start()

            log.info("H2 Console started at ${webServer.getURL()}")
            log.info("Connect to H2 Console: ${webServer.getURL()}")
            log.info("JDBC URL for Orders: jdbc:h2:mem:ordersdb")
            log.info("JDBC URL for Containers: jdbc:h2:mem:containersdb")
            log.info("JDBC URL for Bookings: jdbc:h2:mem:bookingsdb")
            log.info("User: sa, Password: (empty)")

            startPromise.complete()
        } catch (e: Exception) {
            log.error("Failed to start H2 Console: ${e.message}", e)
            startPromise.fail(e)
        }
    }

    override fun stop(stopPromise: Promise<Void>) {
        log.info("Stopping H2 Console.")
        // Los servidores H2 suelen cerrarse con la JVM, pero puedes añadir lógica explícita aquí si es necesario.
        stopPromise.complete()
    }
}