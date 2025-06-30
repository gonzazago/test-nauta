package com.gonzazago.nauta.orders.repository.order

import com.gonzazago.nauta.orders.domain.model.Invoice
import com.gonzazago.nauta.orders.domain.model.Order
import com.gonzazago.nauta.orders.mapper.OrderMapper
import com.gonzazago.nauta.orders.repository.entity.InvoiceEntity
import com.gonzazago.nauta.orders.repository.entity.OrderContainerEntity
import com.gonzazago.nauta.orders.repository.entity.OrderEntity
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.*

class OrderRepositoryImpl(
    private val orderMapper: OrderMapper,
    private val dbClient: SqlClient
) : OrderRepository {

    private val log = LoggerFactory.getLogger(OrderRepositoryImpl::class.java)

    private suspend fun <T> withTransaction(block: suspend (SqlConnection, Transaction) -> T): T {
        val poolClient = dbClient as Pool
        val connection: SqlConnection = poolClient.connection.coAwait()
        val transaction: Transaction = connection.begin().coAwait()

        try {
            val result =
                block(connection, transaction)
            transaction.commit().coAwait()
            return result
        } catch (e: Exception) {
            log.error("Transaction failed, rolling back: ${e.message}", e)
            transaction.rollback().coAwait() // Hace rollback
            throw e // Relanza la excepción
        } finally {
            connection.close().coAwait() // Cierra la conexión
        }
    }


    override suspend fun save(order: Order): Order {
        log.info("OrderRepositoryImpl: Attempting to save order: ${order.purchase}")

        return withTransaction { connection, _ ->

            val orderEntity = orderMapper.toEntity(order)
            insertOrderPurchase(connection, orderEntity)
            insertOrderInvoices(connection, order.invoices)
            insertOrderContainer(connection, order)
            order
        }
    }

    override suspend fun getPurchaseByID(purchaseID: String): Order? {
        val rows = findPurchaseByID(purchaseID)
        val firstRow = rows?.firstOrNull()

        return if (firstRow != null) {
            val invoiceEntity = mapRowToOrderEntity(firstRow)
            orderMapper.toDomainModel(invoiceEntity)
        } else {
            log.info("OrderRepositoryImpl: No invoice found for purchase ID: $purchaseID")
            null
        }
    }

    override suspend fun getPurchaseOrdersByClient(clientID: String): List<Order> {
        val rows = findOrdersByClient(clientID)
        if (rows.count() > 0) {
            rows.forEach{ row ->  println(row.getString(PURCHASE_ORDER_ID)) }
            return rows.map { orderMapper.toDomainModel(mapRowToOrderEntity(it)) }.toList()
        }
        return emptyList()
    }

    override suspend fun getContainersByPurchaseOrder(orderId: String, clientId: String): List<String> {
        val selectOrderContainerSql = "SELECT container_id FROM order_containers WHERE purchase_order_id = ? AND client_id = ?"
        val rows = dbClient.preparedQuery(selectOrderContainerSql).execute(
            Tuple.of(orderId, clientId)
        ).coAwait()
        return rows.map { it.getString(CONTAINER_ID) }
    }

    private suspend fun findOrdersByClient(clientID: String): RowSet<Row> {
        val findContainerByCLientQuery =
            "SELECT * from PURCHASE_ORDERS WHERE CLIENT_ID = ?".trimIndent()
        return dbClient.preparedQuery(findContainerByCLientQuery).execute(Tuple.of(clientID)).coAwait()
    }

    private suspend fun insertOrderPurchase(connection: SqlConnection, order: OrderEntity) {
        if (!checkExistPurchaseByID(connection, order.id)) {
            val insertOrderSql = """
            INSERT INTO purchase_orders (purchase_order_id, client_id, booking_id)
            VALUES (?, ?, ?)
        """.trimIndent()
            val orderParams = Tuple.of(order.id, order.clientId, order.bookingId)
            connection.preparedQuery(insertOrderSql).execute(orderParams).await()
            log.info("OrderRepositoryImpl: Saved purchase_order: ${order.id}")
        }

    }

    private suspend fun insertOrderInvoices(connection: SqlConnection, invoices: List<Invoice>) {
        invoices.forEach { invoice ->
            val invoiceEntity = orderMapper.toEntity(invoice)
            val insertInvoiceSql = """
                INSERT INTO invoices (invoice_id, client_id, purchase_order_id)
                VALUES (?, ?, ?)
            """.trimIndent()
            val invoiceParams = Tuple.of(invoiceEntity.id, invoiceEntity.clientId, invoiceEntity.orderPurchaseId)
            connection.preparedQuery(insertInvoiceSql).execute(invoiceParams).await()
            log.info("OrderRepositoryImpl: Saved invoice: ${invoice.id} for order ${invoice.orderPurchaseId}")

        }
    }

    private suspend fun insertOrderContainer(connection: SqlConnection, order: Order) {

        order.containerIds?.forEach { containerId ->
            val orderContainerEntity = OrderContainerEntity(
                orderPurchaseId = order.purchase,
                containerId = containerId,
                clientId = order.clientId
            )
            val insertOrderContainerSql = """
                    INSERT INTO order_containers (purchase_order_id, container_id, client_id)
                    VALUES (?, ?, ?)
                """.trimIndent()
            val orderContainerParams = Tuple.of(
                orderContainerEntity.orderPurchaseId,
                orderContainerEntity.containerId,
                orderContainerEntity.clientId
            )
            connection.preparedQuery(insertOrderContainerSql).execute(orderContainerParams).coAwait()
            log.info("OrderRepositoryImpl: Saved order_container relation: Order ${order.purchase} - Container ${containerId}")

        }
    }

    private suspend fun checkExistPurchaseByID(connection: SqlConnection, invoice: String): Boolean {
        val findInvoiceQuery =
            "SELECT PURCHASE_ORDER_ID from PURCHASE_ORDERS WHERE PURCHASE_ORDER_ID = ?".trimIndent()
        val rows = connection.preparedQuery(findInvoiceQuery).execute(Tuple.of(invoice)).coAwait()
        return rows.size() > 0
    }

    private suspend fun findPurchaseByID(invoice: String): RowSet<Row>? {
        val findInvoiceQuery =
            "SELECT * from PURCHASE_ORDERS WHERE PURCHASE_ORDER_ID = ?".trimIndent()
        val rows = dbClient.preparedQuery(findInvoiceQuery).execute(Tuple.of(invoice)).coAwait()
        return rows

    }


    internal fun mapRowToOrderEntity(row: Row): OrderEntity {
        log.info("map row")
        return OrderEntity(
            id = row.getString(PURCHASE_ORDER_ID),
            clientId = row.getString(CLIENT_ID),
            bookingId = row.getString(BOOKING_ID)
        )
    }

    private fun mapRowToInvoiceEntity(row: Row): InvoiceEntity {
        return InvoiceEntity(
            id = row.getString(INVOICE_ID),
            clientId = row.getString(CLIENT_ID),
            orderPurchaseId = row.getString(PURCHASE_ORDER_ID)
        )
    }

    private fun mapRowToOrderContainerEntity(row: Row): OrderContainerEntity {
        return OrderContainerEntity(
            orderPurchaseId = row.getString(PURCHASE_ORDER_ID),
            containerId = row.getString(CONTAINER_ID),
            clientId = row.getString(CLIENT_ID)
        )
    }


    companion object {
        val PURCHASE_ORDER_ID = "PURCHASE_ORDER_ID"
        val CLIENT_ID = "CLIENT_ID"
        val BOOKING_ID = "BOOKING_ID"
        val CONTAINER_ID = "CONTAINER_ID"
        val INVOICE_ID = "INVOICE_ID"
    }
}