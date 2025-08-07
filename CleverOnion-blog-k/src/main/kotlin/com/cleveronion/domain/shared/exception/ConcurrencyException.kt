package com.cleveronion.domain.shared.exception

/**
 * 并发异常
 * 
 * 当发生并发冲突时抛出此异常，通常用于乐观锁场景
 */
class ConcurrencyException(
    message: String,
    cause: Throwable? = null
) : DomainException(message, cause) {
    
    companion object {
        /**
         * 创建版本冲突异常
         */
        fun versionConflict(entityType: String, id: Any, expectedVersion: Long, actualVersion: Long): ConcurrencyException {
            return ConcurrencyException(
                "Version conflict for $entityType with id '$id': expected version $expectedVersion, but actual version is $actualVersion"
            )
        }
        
        /**
         * 创建通用并发冲突异常
         */
        fun of(entityType: String, id: Any): ConcurrencyException {
            return ConcurrencyException(
                "$entityType with id '$id' has been modified by another process"
            )
        }
    }
}