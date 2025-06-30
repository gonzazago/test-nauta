package com.gonzazago.nauta.orchestator.modules

import com.gonzazago.nauta.container.repository.container.ContainerRepository
import com.gonzazago.nauta.container.repository.container.ContainerRepositoryImpl
import com.gonzazago.nauta.orchestator.modules.ModuleLoader.loadSqlScript
import com.gonzazago.nauta.orchestator.modules.ModuleLoader.log
import com.gonzazago.nauta.orders.repository.order.OrderRepository
import com.gonzazago.nauta.orders.repository.order.OrderRepositoryImpl
import com.typesafe.config.Config
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import org.koin.core.module.Module


fun Module.orderRepository() {

    single<SqlClient> {
        val vertx = get<Vertx>()
        val config = get<Config>()

        val jdbcConnectOptions = JDBCConnectOptions() // <--- Usar JDBCConnectOptions
            .setJdbcUrl(config.getString("db.order.url"))
            .setUser(config.getString("db.order.user"))
            .setPassword(config.getString("db.order.password"))
        val poolOptions = PoolOptions().setMaxSize(config.getInt("db.order.pool_max_size"))
        val client = JDBCPool.pool(vertx, jdbcConnectOptions, poolOptions)
        client.query(loadSqlScript("db/migrations/V1__orders_initial_schema.sql")).execute()
            .onComplete { ar ->
                if (ar.succeeded()) {
                    log.info("OrderService DB schema created successfully.")
                } else {
                    log.error("Failed to create OrderService DB schema: ${ar.cause().message}", ar.cause())
                }
            }
        client
    }
    single<OrderRepository> {
        val sqlClient = get<SqlClient>()
        OrderRepositoryImpl(get(), sqlClient)
    }
}


fun Module.containerRepository() {

    single {
        val vertx = get<Vertx>()
        val config = get<Config>()

        val mongoConfig = JsonObject()
            .put("host", config.getString("db.container.host"))
            .put("port", config.getInt("db.container.port"))
            .put("db_name", config.getString("db.container.database"))

        val client = MongoClient.createShared(vertx, mongoConfig, "db.container.pool_max_size")
        log.info("ContainerService using MongoDB. Pool: container_mongo_pool")
        client
    }
    single<ContainerRepository> {
        val mongoClient = get<MongoClient>()
        ContainerRepositoryImpl(get(), mongoClient)
    }
}

