package com.cleveronion.common.exception

/**
 * 数据库异常
 * 
 * 当数据库操作失败时抛出此异常
 */
class DatabaseException(
    message: String,
    val operation: String? = null,
    cause: Throwable? = null
) : InfrastructureException(message, cause) {
    
    companion object {
        /**
         * 创建数据库操作异常
         */
        fun of(operation: String, error: String, cause: Throwable? = null): DatabaseException {
            return DatabaseException(
                message = "Database operation '$operation' failed: $error",
                operation = operation,
                cause = cause
            )
        }
        
        /**
         * 创建数据库连接异常
         */
        fun connectionFailed(cause: Throwable): DatabaseException {
            return DatabaseException(
                message = "Failed to connect to database",
                operation = "connection",
                cause = cause
            )
        }
    }
}