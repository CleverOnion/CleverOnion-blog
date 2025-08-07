package com.cleveronion.domain.shared.exception

/**
 * 实体未找到异常
 * 
 * 当尝试访问不存在的实体时抛出此异常
 */
class EntityNotFoundException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {
    
    companion object {
        /**
         * 创建实体未找到异常
         */
        fun of(entityType: String, id: Any): EntityNotFoundException {
            return EntityNotFoundException("$entityType with id '$id' not found")
        }
        
        /**
         * 创建实体未找到异常（带条件）
         */
        fun of(entityType: String, condition: String): EntityNotFoundException {
            return EntityNotFoundException("$entityType not found with condition: $condition")
        }
    }
}