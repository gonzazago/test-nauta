package com.gonzazago.nauta.container.execption

class ContainerServiceException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    companion object {
        const val CONTAINER_NOT_FOUND = "CONTAINER_NOT_FOUND"
        const val EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR"
        const val PERSISTENCE_ERROR = "PERSISTENCE_ERROR"
    }
}