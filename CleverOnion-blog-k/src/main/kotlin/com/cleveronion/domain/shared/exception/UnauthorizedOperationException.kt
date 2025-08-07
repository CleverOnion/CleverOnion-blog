package com.cleveronion.domain.shared.exception

/**
 * 未授权操作异常
 * 
 * 当用户尝试执行没有权限的操作时抛出此异常
 */
class UnauthorizedOperationException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {
    
    companion object {
        /**
         * 创建未授权操作异常
         */
        fun of(operation: String, userId: Any? = null): UnauthorizedOperationException {
            val message = if (userId != null) {
                "User '$userId' is not authorized to perform operation: $operation"
            } else {
                "Not authorized to perform operation: $operation"
            }
            return UnauthorizedOperationException(message)
        }
    }
}