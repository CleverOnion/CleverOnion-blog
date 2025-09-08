package com.cleveronion.blog.common.exception;

/**
 * 重复资源异常
 * 当尝试创建已存在的资源时抛出此异常
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class DuplicateResourceException extends BusinessException {
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
    
    /**
     * 构造函数
     * 
     * @param resourceType 资源类型
     * @param resourceKey 资源标识
     */
    public DuplicateResourceException(String resourceType, Object resourceKey) {
        super("DUPLICATE_RESOURCE", String.format("%s with key '%s' already exists", resourceType, resourceKey));
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super("DUPLICATE_RESOURCE", message, cause);
    }
}