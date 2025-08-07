package com.cleveronion.domain.shared.exception

/**
 * 领域异常基类
 * 
 * 所有领域层的异常都应该继承此类
 * 领域异常表示业务规则违反或领域逻辑错误
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)