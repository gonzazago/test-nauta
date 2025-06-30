package com.gonzazago.nauta.orchestator.modules

import com.gonzazago.nauta.container.routes.ContainerRouter
import com.gonzazago.nauta.orchestator.Server
import com.gonzazago.nauta.orchestator.routes.IngestRouter
import com.gonzazago.nauta.orchestator.routes.Router
import com.gonzazago.nauta.orders.routes.OrderRouter
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

object ModuleLoader {
    private lateinit var vertxInstance: Vertx

    fun loadSqlScript(path: String): String {
        return ModuleLoader::class.java.classLoader.getResource(path)?.readText()
            ?: throw IllegalArgumentException("SQL script not found: $path. Make sure it's in src/main/resources.")
    }

    val log = LoggerFactory.getLogger(ModuleLoader::class.java)

    private val appModule = module(createdAtStart = true) {
        single { vertxInstance }
        single<Config> { ConfigFactory.load() }
        single { Server(get()) }
        single { OrderRouter(get(), get()) }
        single { IngestRouter(get()) }
        single { ContainerRouter(get(),get()) }
        single { Router(get(), get(), get(), get()) }

        handlers()
        useCases()
        services()
        workers()
        orderRepository()
        containerRepository()
        mapper()
        publisher()

    }

    fun init(vertx: Vertx) {
        vertxInstance = vertx

        startKoin {
            modules(appModule)
        }
    }


}
