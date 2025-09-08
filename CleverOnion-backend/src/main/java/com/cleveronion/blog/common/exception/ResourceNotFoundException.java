package com.cleveronion.blog.common.exception;

/**
 * 资源未找到异常
 * 当请求的资源不存在时抛出此异常
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class ResourceNotFoundException extends BusinessException {
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
    
    /**
     * 构造函数
     * 
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super("RESOURCE_NOT_FOUND", String.format("%s with id '%s' not found", resourceType, resourceId));
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super("RESOURCE_NOT_FOUND", message, cause);
    }
}