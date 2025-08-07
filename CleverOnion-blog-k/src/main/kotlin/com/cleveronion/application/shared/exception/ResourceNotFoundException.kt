package com.cleveronion.application.shared.exception

/**
 * 资源未找到异常
 * 
 * 当应用层无法找到请求的资源时抛出此异常
 */
class ResourceNotFoundException(
    message: String,
    val resourceType: String? = null,
    val resourceId: String? = null,
    cause: Throwable? = null
) : ApplicationException(message, cause) {
    
    companion object {
        /**
         * 创建资源未找到异常
         */
        fun of(resourceType: String, resourceId: String): ResourceNotFoundException {
            return ResourceNotFoundException(
                message = "$resourceType with id '$resourceId' not found",
                resourceType = resourceType,
                resourceId = resourceId
            )
        }
        
        /**
         * 创建通用资源未找到异常
         */
        fun of(message: String): ResourceNotFoundException {
            return ResourceNotFoundException(message)
        }
    }
}