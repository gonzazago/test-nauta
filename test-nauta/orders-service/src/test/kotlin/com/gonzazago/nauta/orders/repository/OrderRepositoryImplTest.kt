package com.gonzazago.nauta.orders.repository

import com.gonzazago.nauta.orders.domain.model.Invoice
import com.gonzazago.nauta.orders.domain.model.Order
import com.gonzazago.nauta.orders.mapper.OrderMapper
import com.gonzazago.nauta.orders.repository.entity.InvoiceEntity
import com.gonzazago.nauta.orders.repository.entity.OrderEntity
import com.gonzazago.nauta.orders.repository.order.OrderRepositoryImpl
import com.gonzazago.nauta.orders.repository.order.OrderRepositoryImpl.Companion.BOOKING_ID
import com.gonzazago.nauta.orders.repository.order.OrderRepositoryImpl.Companion.CLIENT_ID
import com.gonzazago.nauta.orders.repository.order.OrderRepositoryImpl.Companion.CONTAINER_ID
import com.gonzazago.nauta.orders.repository.order.OrderRepositoryImpl.Companion.PURCHASE_ORDER_ID
import io.mockk.*
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class OrderRepositoryImplTest {

    private val orderMapper: OrderMapper = mockk()
    private val pool: Pool = mockk()
    private val connection: SqlConnection = mockk()
    private val transaction: Transaction = mockk()
    private val dbClient: SqlClient = pool
    private lateinit var repository: OrderRepositoryImpl


    @BeforeEach
    fun setUp() {
        repository = OrderRepositoryImpl(orderMapper, dbClient)
    }

    @Test
    fun `save should insert order, invoices and containers`() = runTest {

        val invoice = Invoice("I1", "order123", "client1")
        // Arrange
        val order = Order(
            purchase = "order123",
            clientId = "clientA",
            bookingId = "bookingX",
            invoices = listOf(invoice),
            containerIds = listOf("cont1", "cont2")
        )


        val orderEntity = OrderEntity(
            id = order.purchase,
            clientId = order.clientId,
            bookingId = order.bookingId
        )
        val invoiceEntity = InvoiceEntity("I1", "order123", "client1")

        // Mock conexión y transacción
        coEvery { pool.connection } returns Future.succeededFuture(connection)
        coEvery { connection.begin() } returns Future.succeededFuture(transaction)
        coEvery { transaction.commit() } returns Future.succeededFuture()
        coEvery { transaction.rollback() } returns Future.succeededFuture()
        coEvery { connection.close() } returns Future.succeededFuture()

        // Mapper
        coEvery { orderMapper.toEntity(order) } returns orderEntity
        coEvery { orderMapper.toEntity(invoice) } returns invoiceEntity

        // Mock de SELECT existencia de purchase (no existe)
        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()

        val emptyRowSet = mockk<RowSet<Row>> {
            every { size() } returns 0
            every { count() } returns 0
            every { iterator() } returns object : RowIterator<Row> {
                private val inner = emptyList<Row>().iterator()
                override fun hasNext() = inner.hasNext()
                override fun next() = inner.next()
                override fun remove() { /* no-op */
                }
            }
            every { firstOrNull() } returns null
        }

        every {
            connection.preparedQuery(match { it.contains("SELECT PURCHASE_ORDER_ID") })
        } returns selectQuery

        coEvery { selectQuery.execute(any<Tuple>()) } returns Future.succeededFuture(emptyRowSet)

        // Mock de INSERTs
        val insertQuery = mockk<PreparedQuery<RowSet<Row>>>(relaxed = true)
        every { connection.preparedQuery(match { it.contains("INSERT") }) } returns insertQuery
        coEvery { insertQuery.execute(any<Tuple>()) } returns Future.succeededFuture(mockk())

        // Act
        val result = repository.save(order)

        // Assert
        assertEquals(order, result)

        coVerify(atLeast = 1, atMost = 2) { connection.begin() }
        coVerify(exactly = 1) { transaction.commit() }
        coVerify { connection.close() }
        coVerify { connection.preparedQuery(withArg { println("Ejecutando: $it") }) }
    }

    @Test
    fun `findPurchase By id`() = runTest {

        val orderMock = JsonObject()
            .put(PURCHASE_ORDER_ID, "order123")
            .put(CLIENT_ID, "clientA")
            .put(BOOKING_ID, "bookingX")

        val order = Order(
            purchase = "order123",
            clientId = "clientA",
            bookingId = "bookingX",
            invoices = listOf(),
            containerIds = listOf()
        )

        val orderEntity = OrderEntity(
            id = order.purchase,
            clientId = order.clientId,
            bookingId = order.bookingId
        )

        coEvery { orderMapper.toEntity(order) } returns orderEntity
        coEvery {
            orderMapper.toDomainModel(eq(orderEntity))
        } returns order

        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()

        every {
            dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE PURCHASE_ORDER_ID = ?")
        } returns selectQuery

        coEvery { selectQuery.execute(any<Tuple>()) } returns mockedRowSet(listOf(orderMock))

        // Act
        val result = repository.getPurchaseByID("order123")

        // Assert
        assertEquals(order, result)
        coVerify(exactly = 1) {
            dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE PURCHASE_ORDER_ID = ?")
        }
    }

    @Test
    fun `findPurchase By id - not found`() = runTest {

        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()

        val rowSet = mockedEmptyRowSet()

        every {
            dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE PURCHASE_ORDER_ID = ?")
        } returns selectQuery

        coEvery { selectQuery.execute(any<Tuple>()) } returns rowSet

        // Act
        val result = repository.getPurchaseByID("order123")

        // Assert
        assertEquals(null, result)
        coVerify(exactly = 1) {
            dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE PURCHASE_ORDER_ID = ?")
        }
    }

    @Test
    fun `findOrders By Client - empty list`() = runTest {
        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()

        val rowSet = mockedEmptyRowSet()

        every {
            dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE CLIENT_ID = ?")
        } returns selectQuery

        coEvery { selectQuery.execute(any<Tuple>()) } returns rowSet

        // Act
        val result = repository.getPurchaseOrdersByClient("client123")

        // Assert
        assertEquals(emptyList<Order>(), result)
        coVerify(exactly = 1) {
            dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE CLIENT_ID = ?")
        }
    }


    @Test
    fun `findOrders By Client`() = runTest {
        val orderMockMock = JsonObject()
            .put(PURCHASE_ORDER_ID, "order123")
            .put(CLIENT_ID, "clientA")
            .put(BOOKING_ID, "bookingX")


        val order = Order(
            purchase = "order123",
            clientId = "clientA",
            bookingId = "bookingX",
            invoices = listOf(),
            containerIds = listOf()
        )

        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()
        coEvery { dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE CLIENT_ID = ?") } returns selectQuery
        coEvery { selectQuery.execute(any<Tuple>()) } returns mockedRowSet(listOf(orderMockMock))
        every {
            orderMapper.toDomainModel(match {
                it.id == "order123" && it.clientId == "clientA" && it.bookingId == "bookingX"
            })
        } returns order
        val result = repository.getPurchaseOrdersByClient("clientA")
        assertEquals(listOf(order), result)
        coVerify(exactly = 1) { dbClient.preparedQuery("SELECT * from PURCHASE_ORDERS WHERE CLIENT_ID = ?") }
    }


    @Test
    fun `should find container IDs for a purchase order`() = runTest {
        val purchaseId = "PO123"
        val containerId1 = "MEDU1234567"
        val containerId2 = "MEDU1234568"

        val containerMock = JsonObject()
            .put(PURCHASE_ORDER_ID, purchaseId)
            .put(CONTAINER_ID, containerId1)


        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()

        coEvery {
            dbClient.preparedQuery("SELECT container_id FROM order_containers WHERE purchase_order_id = ? AND client_id = ?")
        } returns selectQuery
        coEvery { selectQuery.execute(ofType(Tuple::class)) } returns mockedRowSet(listOf(containerMock))

        val clientId = "CLIENTE_X"
        val result = repository.getContainersByPurchaseOrder(purchaseId, clientId)

        assertNotNull(result)
        assertEquals(listOf(containerId1), result)

        coVerify(exactly = 1) { dbClient.preparedQuery("SELECT container_id FROM order_containers WHERE purchase_order_id = ? AND client_id = ?") }
        val tupleSlot = slot<Tuple>()
        coVerify {
            selectQuery.execute(capture(tupleSlot))
        }
        assertEquals(purchaseId, tupleSlot.captured.getString(0))
        assertEquals(clientId, tupleSlot.captured.getString(1))

    }

    @Test
    fun `should return empty list if no container IDs found`() = runTest {
        val purchaseId = "PO123"
        val orderId = "PO123"
        val clientId = "CLIENTE_X"

        val emptyRowSet = mockedEmptyRowSet()

        val selectQuery = mockk<PreparedQuery<RowSet<Row>>>()

        val selectOrderContainerSql =
            "SELECT container_id FROM order_containers WHERE purchase_order_id = ? AND client_id = ?"

        coEvery { dbClient.preparedQuery(selectOrderContainerSql) } returns selectQuery
        coEvery { selectQuery.execute(ofType(Tuple::class)) } returns emptyRowSet


        val result = repository.getContainersByPurchaseOrder(orderId, clientId)

        assertNotNull(result)
        assertEquals(emptyList<String>(), result)

        // Verificaciones
        coVerify(exactly = 1) { dbClient.preparedQuery(selectOrderContainerSql) }

        val tupleSlot = slot<Tuple>()
        coVerify {
            selectQuery.execute(capture(tupleSlot))
        }
        assertEquals(purchaseId, tupleSlot.captured.getString(0))
        assertEquals(clientId, tupleSlot.captured.getString(1))
    }

    private fun mockedRowSet(list: List<JsonObject>): Future<RowSet<Row>> {
        val rowSet = mockk<RowSet<Row>>()
        every { rowSet.rowCount() } returns list.size
        every { rowSet.size() } returns list.size
        val rowList = list.map { mockRow(it) }
        mockIterator(rowSet, rowList)
        return Future.succeededFuture(rowSet)
    }

    private fun mockedEmptyRowSet(): Future<RowSet<Row>> = mockedRowSet(emptyList())

    private fun mockRow(rowData: JsonObject): Row {
        val row: Row = mockk()
        val slot = slot<String>()
        every { row.getInteger(capture(slot)) } answers { rowData.getInteger(slot.captured) }
        every { row.getLong(capture(slot)) } answers { rowData.getLong(slot.captured) }
        every { row.getDouble(capture(slot)) } answers { rowData.getDouble(slot.captured) }
        every { row.getString(capture(slot)) } answers { rowData.getString(slot.captured) }
        every { row.getLocalTime(capture(slot)) } answers { LocalTime.parse(rowData.getString(slot.captured)) }
        every { row.getLocalDate(capture(slot)) } answers {
            row.getString(slot.captured)?.let {
                LocalDate.parse(rowData.getString(slot.captured))
            }
        }
        every { row.getLocalDateTime(capture(slot)) } answers {
            when (rowData.getString(slot.captured)) {
                null -> null
                else -> LocalDateTime.parse(rowData.getString(slot.captured))
            }
        }
        every { row.getBoolean(capture(slot)) } answers { rowData.getBoolean(slot.captured) }
        every { row.getBigDecimal(capture(slot)) } answers { rowData.getDouble(slot.captured)?.toBigDecimal() }
        every { row.getValue(capture(slot)) } answers { rowData.getValue(slot.captured) }
        return row
    }

    private fun mockIterator(rowSet: RowSet<Row>, rowList: List<Row>) {
        every { rowSet.iterator() } answers {
            val rowListIterator = rowList.iterator()
            val iterator = mockk<RowIterator<Row>>()
            every { iterator.hasNext() } answers { rowListIterator.hasNext() }
            every { iterator.next() } answers { rowListIterator.next() }
            iterator
        }
    }

}