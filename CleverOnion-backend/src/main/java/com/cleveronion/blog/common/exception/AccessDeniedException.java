package com.cleveronion.blog.common.exception;

/**
 * 权限不足异常
 * 当用户没有足够权限访问资源时抛出此异常
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class AccessDeniedException extends BusinessException {
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public AccessDeniedException(String message) {
        super("ACCESS_DENIED", message);
    }
    
    /**
     * 构造函数
     * 
     * @param resource 资源名称
     * @param action 操作名称
     */
    public AccessDeniedException(String resource, String action) {
        super("ACCESS_DENIED", String.format("Access denied for action '%s' on resource '%s'", action, resource));
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public AccessDeniedException(String message, Throwable cause) {
        super("ACCESS_DENIED", message, cause);
    }
}