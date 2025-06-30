package com.gonzazago.nauta.orders.execption

class OrderServiceException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    companion object {
        const val ORDER_NOT_FOUND = "ORDER_NOT_FOUND"
        const val ORDER_ACCESS_DENIED = "ORDER_ACCESS_DENIED" // Para el caso de aislamiento
        const val INVOICE_ALREADY_EXISTS = "INVOICE_ALREADY_EXISTS"
        const val INVALID_ORDER_STATE = "INVALID_ORDER_STATE"
        const val EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR"
        const val PERSISTENCE_ERROR = "PERSISTENCE_ERROR"
    }
}