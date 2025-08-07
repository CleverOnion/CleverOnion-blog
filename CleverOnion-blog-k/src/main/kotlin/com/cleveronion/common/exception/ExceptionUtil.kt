package com.cleveronion.common.exception

import com.cleveronion.application.shared.exception.ApplicationException
import com.cleveronion.domain.shared.exception.DomainException

/**
 * 异常工具类
 * 
 * 提供异常处理的通用工具方法
 */
object ExceptionUtil {
    
    /**
     * 检查异常是否为业务异常（领域异常或应用异常）
     */
    fun isBusinessException(throwable: Throwable): Boolean {
        return throwable is DomainException || throwable is ApplicationException
    }
    
    /**
     * 检查异常是否为技术异常（基础设施异常）
     */
    fun isTechnicalException(throwable: Throwable): Boolean {
        return throwable is InfrastructureException
    }
    
    /**
     * 获取异常的根本原因
     */
    fun getRootCause(throwable: Throwable): Throwable {
        var cause = throwable
        while (cause.cause != null && cause.cause != cause) {
            cause = cause.cause!!
        }
        return cause
    }
    
    /**
     * 获取异常链中的所有异常消息
     */
    fun getAllMessages(throwable: Throwable): List<String> {
        val messages = mutableListOf<String>()
        var current: Throwable? = throwable
        
        while (current != null) {
            current.message?.let { messages.add(it) }
            current = current.cause
            if (current == throwable) break // 避免循环引用
        }
        
        return messages
    }
    
    /**
     * 构建详细的异常信息字符串
     */
    fun buildDetailedMessage(throwable: Throwable): String {
        val messages = getAllMessages(throwable)
        return if (messages.size == 1) {
            messages.first()
        } else {
            messages.joinToString(" -> ")
        }
    }
    
    /**
     * 检查异常链中是否包含特定类型的异常
     */
    inline fun <reified T : Throwable> containsException(throwable: Throwable): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is T) return true
            current = current.cause
            if (current == throwable) break // 避免循环引用
        }
        return false
    }
    
    /**
     * 从异常链中查找特定类型的异常
     */
    inline fun <reified T : Throwable> findException(throwable: Throwable): T? {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is T) return current
            current = current.cause
            if (current == throwable) break // 避免循环引用
        }
        return null
    }
}