package com.cleveronion.common.exception

/**
 * 基础设施异常基类
 * 
 * 所有基础设施层的异常都应该继承此类
 * 基础设施异常表示技术层面的错误，如数据库连接失败、外部服务调用失败等
 */
abstract class InfrastructureException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)