package com.cleveronion.application.shared.exception

/**
 * 应用异常基类
 * 
 * 所有应用层的异常都应该继承此类
 * 应用异常表示应用服务层的错误，通常是用例执行过程中的问题
 */
abstract class ApplicationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)