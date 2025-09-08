package com.cleveronion.blog.common.exception;

/**
 * 参数验证异常
 * 当请求参数验证失败时抛出此异常
 * 
 * @author CleverOnion
 * @since 1.0.0
 */
public class ValidationException extends BusinessException {
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
    
    /**
     * 构造函数
     * 
     * @param field 字段名
     * @param value 字段值
     * @param reason 验证失败原因
     */
    public ValidationException(String field, Object value, String reason) {
        super("VALIDATION_ERROR", String.format("Field '%s' with value '%s' is invalid: %s", field, value, reason));
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}