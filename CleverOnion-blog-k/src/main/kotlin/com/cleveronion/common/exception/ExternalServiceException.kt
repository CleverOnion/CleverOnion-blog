package com.cleveronion.common.exception

/**
 * 外部服务异常
 * 
 * 当调用外部服务失败时抛出此异常
 */
class ExternalServiceException(
    message: String,
    val serviceName: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : InfrastructureException(message, cause) {
    
    companion object {
        /**
         * 创建外部服务调用异常
         */
        fun of(serviceName: String, error: String, statusCode: Int? = null, cause: Throwable? = null): ExternalServiceException {
            val message = if (statusCode != null) {
                "External service '$serviceName' call failed with status $statusCode: $error"
            } else {
                "External service '$serviceName' call failed: $error"
            }
            return ExternalServiceException(
                message = message,
                serviceName = serviceName,
                statusCode = statusCode,
                cause = cause
            )
        }
        
        /**
         * 创建服务不可用异常
         */
        fun serviceUnavailable(serviceName: String, cause: Throwable? = null): ExternalServiceException {
            return ExternalServiceException(
                message = "External service '$serviceName' is unavailable",
                serviceName = serviceName,
                statusCode = 503,
                cause = cause
            )
        }
    }
}